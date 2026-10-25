package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// "usuario" acepta el username o el correo electrónico.
public record LoginRestRequest(
    @NotBlank String usuario,
    @NotBlank @Size(min = 8) String password) {}
