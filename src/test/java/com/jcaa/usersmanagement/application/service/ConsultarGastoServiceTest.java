package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.service.dto.query.ConsultarGastoQuery;
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
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para ConsultarGastoService: el dueño, el ADMIN y el REVIEWER ven el gasto; otro MEMBER
 * recibe 'no encontrado', igual que si no existiera.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsultarGastoService")
class ConsultarGastoServiceTest {

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

  private ConsultarGastoService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service = new ConsultarGastoService(getGastoByIdPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("el dueño consulta su gasto")
  void shouldReturnGastoToOwner() {
    // Arrange
    final GastoDetalle detalle = gastoDeAna();
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(detalle));

    // Act & Assert
    assertSame(detalle, service.execute(new ConsultarGastoQuery(ANA, GASTO_ID)));
  }

  @Test
  @DisplayName("el REVIEWER consulta un gasto ajeno")
  void shouldReturnGastoToReviewer() {
    // Arrange
    final GastoDetalle detalle = gastoDeAna();
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(detalle));

    // Act & Assert
    assertSame(detalle, service.execute(new ConsultarGastoQuery(REVISOR, GASTO_ID)));
  }

  @Test
  @DisplayName("otro MEMBER recibe 'no encontrado'")
  void shouldHideGastoFromOtherMember() {
    // Arrange
    when(getGastoByIdPort.getById(new GastoId(GASTO_ID))).thenReturn(Optional.of(gastoDeAna()));

    // Act & Assert
    assertThrows(
        GastoNotFoundException.class,
        () -> service.execute(new ConsultarGastoQuery(LUIS, GASTO_ID)));
  }
}
