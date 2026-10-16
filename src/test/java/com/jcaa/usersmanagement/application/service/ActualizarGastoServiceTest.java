package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.UpdateGastoPort;
import com.jcaa.usersmanagement.application.service.dto.command.ActualizarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.exception.GastoAccessDeniedException;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para ActualizarGastoService.
 *
 * <p>Verifica las reglas de acceso: el dueño y el ADMIN actualizan; otro MEMBER recibe 404 (no
 * puede ni ver el gasto); el REVIEWER recibe 403 (lo ve, pero no puede modificarlo). El IVA se
 * recalcula y el dueño original se conserva.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ActualizarGastoService")
class ActualizarGastoServiceTest {

  private static final String GASTO_ID = "g-001";
  private static final Solicitante ANA = new Solicitante(new UserId("u-ana"), UserRole.MEMBER);
  private static final Solicitante LUIS = new Solicitante(new UserId("u-luis"), UserRole.MEMBER);
  private static final Solicitante REVISOR =
      new Solicitante(new UserId("u-rev"), UserRole.REVIEWER);
  private static final Solicitante ADMIN = new Solicitante(new UserId("u-adm"), UserRole.ADMIN);

  // Gasto de Ana: 100 sin IVA → 19 de IVA → 119 en total
  private static GastoDetalle gastoDeAna() {
    final GastoModel gasto =
        GastoModel.registrar(
            new GastoId(GASTO_ID),
            ANA.id(),
            new GastoFecha(LocalDate.of(2026, 10, 1)),
            Dinero.of("100"),
            new GastoLugar("Exito"),
            new GastoDescripcion("Mercado"));
    return new GastoDetalle(gasto, "Ana Torres");
  }

  @Mock private GetGastoByIdPort getGastoByIdPort;
  @Mock private UpdateGastoPort updateGastoPort;

  private ActualizarGastoService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new ActualizarGastoService(
              getGastoByIdPort, updateGastoPort, validatorFactory.getValidator());
    }
  }

  private static ActualizarGastoCommand comandoDe(final Solicitante solicitante) {
    return new ActualizarGastoCommand(
        solicitante, GASTO_ID, LocalDate.of(2026, 10, 2), new BigDecimal("200"), "Olimpica", "Aseo");
  }

  @Test
  @DisplayName("el dueño actualiza su gasto y el IVA se recalcula")
  void shouldLetOwnerUpdateAndRecalculateVat() {
    // Arrange
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(gastoDeAna()));
    final ArgumentCaptor<GastoModel> captor = ArgumentCaptor.forClass(GastoModel.class);

    // Act
    final GastoDetalle result = service.execute(comandoDe(ANA));

    // Assert
    verify(updateGastoPort).update(captor.capture());
    final GastoModel actualizado = captor.getValue();
    assertAll(
        () -> assertEquals(Dinero.of("38.00"), actualizado.getIvaTotal()),
        () -> assertEquals(Dinero.of("238.00"), actualizado.getValorTotalConIva()),
        () -> assertEquals("Olimpica", actualizado.getLugar().value()),
        () -> assertEquals("Ana Torres", result.nombreUsuario()));
  }

  @Test
  @DisplayName("el ADMIN actualiza un gasto ajeno y el dueño original se conserva")
  void shouldLetAdminUpdateAnyGastoKeepingOwner() {
    // Arrange
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(gastoDeAna()));
    final ArgumentCaptor<GastoModel> captor = ArgumentCaptor.forClass(GastoModel.class);

    // Act
    service.execute(comandoDe(ADMIN));

    // Assert
    verify(updateGastoPort).update(captor.capture());
    assertEquals(ANA.id(), captor.getValue().getUserId(), "el dueño sigue siendo Ana");
  }

  @Test
  @DisplayName("otro MEMBER recibe 'no encontrado' y no se actualiza nada")
  void shouldHideGastoFromOtherMember() {
    // Arrange
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(gastoDeAna()));

    // Act & Assert
    assertThrows(GastoNotFoundException.class, () -> service.execute(comandoDe(LUIS)));
    verifyNoInteractions(updateGastoPort);
  }

  @Test
  @DisplayName("el REVIEWER recibe 'acceso denegado' sobre un gasto ajeno")
  void shouldDenyReviewerUpdatingOthersGasto() {
    // Arrange
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(gastoDeAna()));

    // Act & Assert
    assertThrows(GastoAccessDeniedException.class, () -> service.execute(comandoDe(REVISOR)));
    verifyNoInteractions(updateGastoPort);
  }

  @Test
  @DisplayName("un gasto inexistente produce 'no encontrado'")
  void shouldFailWhenGastoDoesNotExist() {
    // Arrange
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(GastoNotFoundException.class, () -> service.execute(comandoDe(ADMIN)));
    verifyNoInteractions(updateGastoPort);
  }
}
