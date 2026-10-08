package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ─────────────────────────────────────────────────────────────────────────────
// Cuerpo del registro público (POST /api/auth/register).
// No incluye id (lo genera el servidor) ni rol: el registro público siempre
// crea usuarios MEMBER, para que nadie pueda registrarse como ADMIN.
// ─────────────────────────────────────────────────────────────────────────────
public record RegisterRestRequest(
    @NotBlank(message = "name must not be blank")
        @Size(min = 3, message = "name must have at least 3 characters")
        String name,
    @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email,
    @NotBlank(message = "password must not be blank")
        @Size(min = 8, message = "password must have at least 8 characters")
        String password) {}