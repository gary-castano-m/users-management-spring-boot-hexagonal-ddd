package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Cuerpo de POST /api/auth/forgot-password: solo el correo del usuario.
public record ForgotPasswordRestRequest(
    @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email) {}
