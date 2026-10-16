package com.jcaa.usersmanagement.application.service.dto.command;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

// Datos para actualizar un gasto existente. El IVA se recalcula en el dominio.
public record ActualizarGastoCommand(
    @NotNull(message = "solicitante must not be null") Solicitante solicitante,
    @NotBlank(message = "gastoId must not be blank") String gastoId,
    @NotNull(message = "fecha must not be null") LocalDate fecha,
    @NotNull(message = "valorTotalSinIva must not be null") BigDecimal valorTotalSinIva,
    @NotBlank(message = "lugar must not be blank") String lugar,
    @NotBlank(message = "descripcion must not be blank") String descripcion) {}
