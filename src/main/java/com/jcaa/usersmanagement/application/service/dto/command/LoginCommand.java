package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ─────────────────────────────────────────────────────────────────────────────
// Datos para iniciar sesión. "usuario" acepta el username o el correo:
// si contiene "@" se trata como correo; si no, como username.
// ─────────────────────────────────────────────────────────────────────────────
public record LoginCommand(
    @NotBlank(message = "usuario must not be blank") String usuario,
    @NotBlank(message = "password must not be blank")
        @Size(min = 8, message = "password must have at least 8 characters")
        String password) {}
