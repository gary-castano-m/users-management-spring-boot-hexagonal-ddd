package com.jcaa.usersmanagement.application.service.dto.command;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos para eliminar un gasto.
public record EliminarGastoCommand(
    @NotNull(message = "solicitante must not be null") Solicitante solicitante,
    @NotBlank(message = "gastoId must not be blank") String gastoId) {}
