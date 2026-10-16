package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveGastoPort;
import com.jcaa.usersmanagement.application.service.dto.command.RegistrarGastoCommand;
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
import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import jakarta.validation.ConstraintViolationException;
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
 * Tests para RegistrarGastoService.
 *
 * <p>Verifica que el dueño del gasto es siempre el solicitante, que el IVA lo calcula el dominio y
 * que los datos inválidos se rechazan sin guardar nada.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarGastoService")
class RegistrarGastoServiceTest {

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

  @Mock private SaveGastoPort saveGastoPort;
  @Mock private GetGastoByIdPort getGastoByIdPort;

  private RegistrarGastoService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new RegistrarGastoService(
              saveGastoPort, getGastoByIdPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("execute() guarda el gasto a nombre del solicitante con el IVA calculado")
  void shouldRegisterGastoForRequesterWithVat() {
    // Arrange
    final GastoDetalle guardado = gastoDeAna();
    when(getGastoByIdPort.getById(any(GastoId.class))).thenReturn(Optional.of(guardado));
    final ArgumentCaptor<GastoModel> captor = ArgumentCaptor.forClass(GastoModel.class);

    // Act
    final GastoDetalle result =
        service.execute(
            new RegistrarGastoCommand(
                ANA, LocalDate.of(2026, 10, 1), new BigDecimal("10050"), "Exito", "Mercado"));

    // Assert
    verify(saveGastoPort).save(captor.capture());
    final GastoModel gasto = captor.getValue();
    assertAll(
        "gasto registrado",
        () -> assertEquals(ANA.id(), gasto.getUserId(), "el dueño es el solicitante"),
        () -> assertEquals(Dinero.of("1909.50"), gasto.getIvaTotal()),
        () -> assertEquals(Dinero.of("11959.50"), gasto.getValorTotalConIva()),
        () -> assertSame(guardado, result, "devuelve el gasto leído después de guardar"));
    verify(getGastoByIdPort).getById(gasto.getId());
  }

  @Test
  @DisplayName("execute() rechaza un comando sin fecha y no guarda nada")
  void shouldRejectCommandWithoutDate() {
    // Act & Assert
    assertThrows(
        ConstraintViolationException.class,
        () ->
            service.execute(
                new RegistrarGastoCommand(ANA, null, new BigDecimal("100"), "Exito", "Mercado")));
    verifyNoInteractions(saveGastoPort, getGastoByIdPort);
  }

  @Test
  @DisplayName("execute() rechaza un valor negativo y no guarda nada")
  void shouldRejectNegativeValue() {
    // Act & Assert
    assertThrows(
        InvalidGastoException.class,
        () ->
            service.execute(
                new RegistrarGastoCommand(
                    ANA, LocalDate.of(2026, 10, 1), new BigDecimal("-1"), "Exito", "Mercado")));
    verifyNoInteractions(saveGastoPort, getGastoByIdPort);
  }
}
