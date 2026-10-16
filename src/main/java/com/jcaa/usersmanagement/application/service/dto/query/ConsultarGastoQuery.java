package com.jcaa.usersmanagement.application.service.dto.query;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datos para consultar un gasto por su id.
public record ConsultarGastoQuery(
    @NotNull(message = "solicitante must not be null") Solicitante solicitante,
    @NotBlank(message = "gastoId must not be blank") String gastoId) {}
