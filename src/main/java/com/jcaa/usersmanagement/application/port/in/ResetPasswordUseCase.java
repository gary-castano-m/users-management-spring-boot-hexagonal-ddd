package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.ResetPasswordCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Puerto de entrada: recuperar la contraseña enviando una temporal por correo.
public interface ResetPasswordUseCase {
  void execute(@NotNull @Valid ResetPasswordCommand command);
}
