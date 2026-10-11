package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Datos de entrada para recuperar la contraseña: solo el correo del usuario.
public record ResetPasswordCommand(
    @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email) {}
