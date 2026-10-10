package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ─────────────────────────────────────────────────────────────────────────────
// Cuerpo para editar el perfil propio (PUT /api/users/me).
// No incluye rol ni estado: un usuario no puede cambiárselos a sí mismo.
// La contraseña es opcional: si se omite o va vacía, se conserva la actual.
// ─────────────────────────────────────────────────────────────────────────────
public record UpdateProfileRestRequest(
    @NotBlank(message = "name must not be blank")
        @Size(min = 3, message = "name must have at least 3 characters")
        String name,
    @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email,
    String password) {}
