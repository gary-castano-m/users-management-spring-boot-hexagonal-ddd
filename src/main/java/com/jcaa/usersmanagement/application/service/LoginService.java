package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.LoginUseCase;
import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.GetUserByUsernamePort;
import com.jcaa.usersmanagement.application.service.dto.command.LoginCommand;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.InvalidUserEmailException;
import com.jcaa.usersmanagement.domain.exception.InvalidUserUsernameException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserUsername;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

  private static final String EMAIL_MARKER = "@";

  private final GetUserByEmailPort getUserByEmailPort;
  private final GetUserByUsernamePort getUserByUsernamePort;
  private final Validator validator;

  @Override
  public UserModel execute(final LoginCommand command) {
    validateCommand(command);

    final UserModel user = findUserOrFailWithInvalidCredentials(command.usuario());

    verifyPasswordOrFail(command.password(), user);
    ensureUserIsActiveOrFail(user);

    return user;
  }

  private void validateCommand(final LoginCommand command) {
    final Set<ConstraintViolation<LoginCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  // Si el texto contiene "@" se busca por correo; si no, por username.
  // Un valor con formato inválido se responde igual que unas credenciales
  // incorrectas, para no revelar qué usuarios existen.
  private UserModel findUserOrFailWithInvalidCredentials(final String usuario) {
    final Optional<UserModel> found;
    try {
      found =
          usuario.contains(EMAIL_MARKER)
              ? getUserByEmailPort.getByEmail(new UserEmail(usuario))
              : getUserByUsernamePort.getByUsername(new UserUsername(usuario));
    } catch (final InvalidUserEmailException | InvalidUserUsernameException exception) {
      throw InvalidCredentialsException.becauseCredentialsAreInvalid();
    }
    return found.orElseThrow(InvalidCredentialsException::becauseCredentialsAreInvalid);
  }

  private static void verifyPasswordOrFail(final String plainPassword, final UserModel user) {
    if (!user.getPassword().verifyPlain(plainPassword)) {
      throw InvalidCredentialsException.becauseCredentialsAreInvalid();
    }
  }

  private static void ensureUserIsActiveOrFail(final UserModel user) {
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw InvalidCredentialsException.becauseUserIsNotActive();
    }
  }
}
