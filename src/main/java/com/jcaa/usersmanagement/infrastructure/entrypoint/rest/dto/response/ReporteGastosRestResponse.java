package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Respuesta de los reportes: totales y el detalle de los gastos encontrados.
public record ReporteGastosRestResponse(
    int cantidad,
    BigDecimal totalSinIva,
    BigDecimal totalIva,
    BigDecimal totalConIva,
    List<GastoRestResponse> gastos) {}
