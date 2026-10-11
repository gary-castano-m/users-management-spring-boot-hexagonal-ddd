package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.ResetPasswordUseCase;
import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.UpdateUserPort;
import com.jcaa.usersmanagement.application.service.dto.command.ResetPasswordCommand;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// ─────────────────────────────────────────────────────────────────────────────
// Recuperación de contraseña con contraseña temporal.
// Las contraseñas se guardan con BCrypt (hash de una sola vía), así que no se
// pueden "recordar": se genera una nueva, se guarda su hash y se envía por
// correo. Si el correo no existe o el usuario no está activo, no se hace nada
// y NO se lanza error: así nadie puede averiguar qué correos están registrados
// (protección contra enumeración de usuarios).
// ─────────────────────────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

  private final GetUserByEmailPort getUserByEmailPort;
  private final UpdateUserPort updateUserPort;
  private final TemporaryPasswordGenerator temporaryPasswordGenerator;
  private final EmailNotificationService emailNotificationService;
  private final Validator validator;

  @Override
  public void execute(final ResetPasswordCommand command) {
    validateCommand(command);

    final UserEmail email = new UserEmail(command.email());
    getUserByEmailPort
        .getByEmail(email)
        .filter(user -> user.getStatus() == UserStatus.ACTIVE)
        .ifPresent(this::resetPassword);
  }

  private void resetPassword(final UserModel user) {
    final String temporaryPassword = temporaryPasswordGenerator.generate();
    final UserModel updated =
        updateUserPort.update(user.changePassword(UserPassword.fromPlainText(temporaryPassword)));
    emailNotificationService.notifyPasswordReset(updated, temporaryPassword);
  }

  private void validateCommand(final ResetPasswordCommand command) {
    final Set<ConstraintViolation<ResetPasswordCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
