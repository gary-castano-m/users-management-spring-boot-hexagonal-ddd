package com.jcaa.usersmanagement.application.service.dto.result;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import java.util.List;
import java.util.Objects;

// ─────────────────────────────────────────────────────────────────────────────
// Resultado de un listado o reporte: los gastos y sus totales.
// Los totales se suman con Dinero (BigDecimal), así son exactos.
// ─────────────────────────────────────────────────────────────────────────────
public record ReporteGastos(
    List<GastoDetalle> gastos,
    int cantidad,
    Dinero totalSinIva,
    Dinero totalIva,
    Dinero totalConIva) {

  public ReporteGastos {
    gastos = List.copyOf(Objects.requireNonNull(gastos, "gastos cannot be null"));
  }

  public static ReporteGastos de(final List<GastoDetalle> gastos) {
    Dinero sinIva = Dinero.CERO;
    Dinero iva = Dinero.CERO;
    Dinero conIva = Dinero.CERO;
    for (final GastoDetalle detalle : gastos) {
      final GastoModel gasto = detalle.gasto();
      sinIva = sinIva.sumar(gasto.getValorTotalSinIva());
      iva = iva.sumar(gasto.getIvaTotal());
      conIva = conIva.sumar(gasto.getValorTotalConIva());
    }
    return new ReporteGastos(gastos, gastos.size(), sinIva, iva, conIva);
  }
}
