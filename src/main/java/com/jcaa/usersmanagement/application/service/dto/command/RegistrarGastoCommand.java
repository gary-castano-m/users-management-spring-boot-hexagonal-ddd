package com.jcaa.usersmanagement.application.service.dto.command;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

// Datos para registrar un gasto. Solo llega el valor SIN IVA: el dominio calcula el resto.
// El dueño será el solicitante (sale del token), nunca un id enviado por el cliente.
public record RegistrarGastoCommand(
    @NotNull(message = "solicitante must not be null") Solicitante solicitante,
    @NotNull(message = "fecha must not be null") LocalDate fecha,
    @NotNull(message = "valorTotalSinIva must not be null") BigDecimal valorTotalSinIva,
    @NotBlank(message = "lugar must not be blank") String lugar,
    @NotBlank(message = "descripcion must not be blank") String descripcion) {}
