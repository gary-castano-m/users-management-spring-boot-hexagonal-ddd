package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.SearchGastosPort;
import com.jcaa.usersmanagement.application.service.dto.query.BuscarGastosQuery;
import com.jcaa.usersmanagement.application.service.dto.result.ReporteGastos;
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
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para BuscarGastosService.
 *
 * <p>Verifica que un MEMBER solo busca entre sus gastos, que ADMIN y REVIEWER buscan entre todos,
 * la normalización del lugar, el rechazo de un rango de fechas invertido y el cálculo de totales.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BuscarGastosService")
class BuscarGastosServiceTest {

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

  private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
  private static final LocalDate HASTA = LocalDate.of(2026, 10, 31);

  @Mock private SearchGastosPort searchGastosPort;

  private BuscarGastosService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service = new BuscarGastosService(searchGastosPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("un MEMBER solo busca entre sus propios gastos")
  void shouldFilterByOwnerForMember() {
    // Act
    service.execute(new BuscarGastosQuery(ANA, null, null, null));

    // Assert
    verify(searchGastosPort).search(ANA.id(), null, null, null);
  }

  @Test
  @DisplayName("el ADMIN busca entre todos, con fechas y lugar sin espacios")
  void shouldSearchAllForAdminWithFilters() {
    // Act
    service.execute(new BuscarGastosQuery(ADMIN, DESDE, HASTA, "  exito "));

    // Assert
    verify(searchGastosPort).search(null, DESDE, HASTA, "exito");
  }

  @Test
  @DisplayName("el REVIEWER busca entre todos y un lugar vacío es 'sin filtro'")
  void shouldSearchAllForReviewerIgnoringBlankPlace() {
    // Act
    service.execute(new BuscarGastosQuery(REVISOR, null, null, "   "));

    // Assert
    verify(searchGastosPort).search(null, null, null, null);
  }

  @Test
  @DisplayName("devuelve el reporte con la cantidad y los totales")
  void shouldReturnReportWithTotals() {
    // Arrange
    when(searchGastosPort.search(any(), any(), any(), any()))
        .thenReturn(List.of(gastoDeAna(), gastoDeAna()));

    // Act
    final ReporteGastos reporte = service.execute(new BuscarGastosQuery(ADMIN, null, null, null));

    // Assert
    assertAll(
        () -> assertEquals(2, reporte.cantidad()),
        () -> assertEquals(Dinero.of("200.00"), reporte.totalSinIva()),
        () -> assertEquals(Dinero.of("38.00"), reporte.totalIva()),
        () -> assertEquals(Dinero.of("238.00"), reporte.totalConIva()));
  }

  @Test
  @DisplayName("rechaza un rango con 'desde' posterior a 'hasta' sin consultar la base de datos")
  void shouldRejectInvertedDateRange() {
    // Act & Assert
    assertThrows(
        InvalidGastoException.class,
        () -> service.execute(new BuscarGastosQuery(ADMIN, HASTA, DESDE, null)));
    verifyNoInteractions(searchGastosPort);
  }
}
