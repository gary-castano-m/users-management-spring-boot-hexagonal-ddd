package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.ResetPasswordUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.ResetPasswordCommand;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.ForgotPasswordRestRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// ─────────────────────────────────────────────────────────────────────────────
// Recuperación de contraseña (público). Responde SIEMPRE 204, exista o no el
// correo, para no revelar qué correos están registrados. La app debe mostrar:
// "Si el correo está registrado, recibirás una contraseña temporal".
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgotPasswordRestController {

  private final ResetPasswordUseCase resetPasswordUseCase;

  @PostMapping("/forgot-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Recuperar contraseña: envía una contraseña temporal al correo")
  public void forgotPassword(@Valid @RequestBody final ForgotPasswordRestRequest request) {
    resetPasswordUseCase.execute(new ResetPasswordCommand(request.email()));
  }
}
