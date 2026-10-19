package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

// ─────────────────────────────────────────────────────────────────────────────
// Cuerpo de POST /api/gastos y PUT /api/gastos/{id}.
// Solo se envía el valor SIN IVA: el IVA y el total los calcula el backend.
// No incluye el dueño: sale del token. Ejemplo:
//   { "fecha": "2026-10-01", "valorTotalSinIva": 10050,
//     "lugar": "Exito San Diego", "descripcion": "Mercado de la semana" }
// ─────────────────────────────────────────────────────────────────────────────
public record GastoRestRequest(
    @NotNull(message = "fecha must not be null") LocalDate fecha,
    @NotNull(message = "valorTotalSinIva must not be null")
        @DecimalMin(value = "0.00", message = "valorTotalSinIva must not be negative")
        BigDecimal valorTotalSinIva,
    @NotBlank(message = "lugar must not be blank")
        @Size(max = 100, message = "lugar must have at most 100 characters")
        String lugar,
    @NotBlank(message = "descripcion must not be blank")
        @Size(max = 255, message = "descripcion must have at most 255 characters")
        String descripcion) {}
