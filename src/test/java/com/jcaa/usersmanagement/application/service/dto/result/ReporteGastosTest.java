package com.jcaa.usersmanagement.application.service.dto.result;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests para ReporteGastos: cantidad y totales exactos de un listado. */
@DisplayName("ReporteGastos")
class ReporteGastosTest {

  @Test
  @DisplayName("de() suma con exactitud los valores sin IVA, el IVA y el total")
  void shouldSumTotals() {
    // Arrange
    final List<GastoDetalle> gastos = List.of(detalle("g-1", "10050"), detalle("g-2", "100"));

    // Act
    final ReporteGastos reporte = ReporteGastos.de(gastos);

    // Assert
    assertAll(
        () -> assertEquals(2, reporte.cantidad()),
        () -> assertEquals(Dinero.of("10150.00"), reporte.totalSinIva()),
        () -> assertEquals(Dinero.of("1928.50"), reporte.totalIva()),
        () -> assertEquals(Dinero.of("12078.50"), reporte.totalConIva()));
  }

  @Test
  @DisplayName("de() con una lista vacía devuelve totales en cero")
  void shouldReturnZerosForEmptyList() {
    // Act
    final ReporteGastos reporte = ReporteGastos.de(List.of());

    // Assert
    assertAll(
        () -> assertEquals(0, reporte.cantidad()),
        () -> assertTrue(reporte.gastos().isEmpty()),
        () -> assertEquals(Dinero.CERO, reporte.totalConIva()));
  }

  private static GastoDetalle detalle(final String id, final String valorSinIva) {
    final GastoModel gasto =
        GastoModel.registrar(
            new GastoId(id),
            new UserId("u-001"),
            new GastoFecha(LocalDate.of(2026, 10, 1)),
            Dinero.of(valorSinIva),
            new GastoLugar("Exito"),
            new GastoDescripcion("Mercado"));
    return new GastoDetalle(gasto, "Ana Torres");
  }
}
