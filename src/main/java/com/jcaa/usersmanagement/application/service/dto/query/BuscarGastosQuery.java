package com.jcaa.usersmanagement.application.service.dto.query;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// ─────────────────────────────────────────────────────────────────────────────
// Criterios del listado y de los reportes de gastos. Todos opcionales:
//   desde/hasta → reporte 1, gastos en un rango de fechas (inclusive)
//   lugar       → reporte 2, gastos por lugar (coincidencia parcial)
// Sin criterios es el listado completo (de los gastos que el solicitante puede ver).
// ─────────────────────────────────────────────────────────────────────────────
public record BuscarGastosQuery(
    @NotNull(message = "solicitante must not be null") Solicitante solicitante,
    LocalDate desde,
    LocalDate hasta,
    String lugar) {}
