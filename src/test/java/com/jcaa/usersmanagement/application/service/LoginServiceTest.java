package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.GetUserByUsernamePort;
import com.jcaa.usersmanagement.application.service.dto.command.LoginCommand;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserUsername;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para LoginService.
 *
 * <p>Cubre: login exitoso (por correo y por username), usuario no encontrado, contraseña
 * incorrecta, usuario inactivo/bloqueado, formato inválido y validación del command.
 */
@DisplayName("LoginService")
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock private GetUserByEmailPort getUserByEmailPort;
  @Mock private GetUserByUsernamePort getUserByUsernamePort;

  private LoginService service;

  private static final String EMAIL = "john@example.com";
  private static final String USERNAME = "john.arrieta";
  private static final String PASSWORD = "SecurePass1";

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new LoginService(
              getUserByEmailPort, getUserByUsernamePort, validatorFactory.getValidator());
    }
  }

  // ── flujo feliz

  @Test
  @DisplayName("execute() retorna el usuario cuando las credenciales son correctas y está activo")
  void shouldReturnUserWhenCredentialsAreValidAndUserIsActive() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);

    final UserModel activeUser =
        new UserModel(
            new UserId("u-001"),
            new UserUsername("john.arrieta"),
            new UserName("John Arrieta"),
            new UserEmail(EMAIL),
            UserPassword.fromPlainText(PASSWORD),
            UserRole.ADMIN,
            UserStatus.ACTIVE);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(activeUser));

    // Act
    final UserModel result = service.execute(command);

    // Assert
    assertSame(activeUser, result, "debe retornar el usuario autenticado");
  }

  // ── email no registrado

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando el email no existe")
  void shouldThrowWhenEmailNotFound() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  // ── contraseña incorrecta

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando la contraseña no coincide")
  void shouldThrowWhenPasswordIsWrong() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, "WrongPass99");

    final UserModel user =
        new UserModel(
            new UserId("u-001"),
            new UserUsername("john.arrieta"),
            new UserName("John Arrieta"),
            new UserEmail(EMAIL),
            UserPassword.fromPlainText(PASSWORD),
            UserRole.MEMBER,
            UserStatus.ACTIVE);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  // ── usuario no activo (PENDING, INACTIVE, BLOCKED)

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando el usuario no está ACTIVE")
  void shouldThrowWhenUserIsNotActive() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);

    final UserModel pendingUser =
        new UserModel(
            new UserId("u-001"),
            new UserUsername("john.arrieta"),
            new UserName("John Arrieta"),
            new UserEmail(EMAIL),
            UserPassword.fromPlainText(PASSWORD),
            UserRole.MEMBER,
            UserStatus.PENDING);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(pendingUser));

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  // ── validación del command

  @Test
  @DisplayName(
      "execute() lanza ConstraintViolationException cuando el command tiene campos inválidos")
  void shouldThrowWhenCommandIsInvalid() {
    // Arrange — usuario en blanco y password muy corto
    final LoginCommand command = new LoginCommand(" ", "short");

    // Act & Assert
    assertThrows(ConstraintViolationException.class, () -> service.execute(command));
    verifyNoInteractions(getUserByEmailPort, getUserByUsernamePort);
  }

  // ── login por username

  @Test
  @DisplayName("execute() busca por username cuando el texto no contiene '@'")
  void shouldLoginWithUsername() {
    // Arrange — se escribe con mayúsculas: debe normalizarse a minúsculas
    final LoginCommand command = new LoginCommand("John.Arrieta", PASSWORD);
    final UserModel activeUser = activeUser();
    when(getUserByUsernamePort.getByUsername(new UserUsername(USERNAME)))
        .thenReturn(Optional.of(activeUser));

    // Act
    final UserModel result = service.execute(command);

    // Assert
    assertSame(activeUser, result, "debe retornar el usuario autenticado");
    verifyNoInteractions(getUserByEmailPort);
  }

  @Test
  @DisplayName("execute() busca por correo cuando el texto contiene '@'")
  void shouldLoginWithEmailWithoutTouchingUsernamePort() {
    // Arrange
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
    when(getUserByEmailPort.getByEmail(new UserEmail(EMAIL)))
        .thenReturn(Optional.of(activeUser()));

    // Act
    service.execute(command);

    // Assert
    verifyNoInteractions(getUserByUsernamePort);
  }

  @Test
  @DisplayName("execute() lanza InvalidCredentialsException cuando el username no existe")
  void shouldThrowWhenUsernameNotFound() {
    // Arrange
    final LoginCommand command = new LoginCommand("nadie", PASSWORD);
    when(getUserByUsernamePort.getByUsername(any())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
  }

  @Test
  @DisplayName(
      "execute() responde credenciales inválidas (no error de formato) con un username mal formado")
  void shouldThrowInvalidCredentialsWhenUsernameFormatIsInvalid() {
    // Arrange — "ana torres" tiene un espacio: no puede ser un username
    final LoginCommand command = new LoginCommand("ana torres", PASSWORD);

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
    verifyNoInteractions(getUserByEmailPort, getUserByUsernamePort);
  }

  @Test
  @DisplayName(
      "execute() responde credenciales inválidas (no error de formato) con un correo mal formado")
  void shouldThrowInvalidCredentialsWhenEmailFormatIsInvalid() {
    // Arrange — contiene "@" pero no es un correo válido
    final LoginCommand command = new LoginCommand("ana@", PASSWORD);

    // Act & Assert
    assertThrows(InvalidCredentialsException.class, () -> service.execute(command));
    verifyNoInteractions(getUserByEmailPort, getUserByUsernamePort);
  }

  private static UserModel activeUser() {
    return new UserModel(
        new UserId("u-001"),
        new UserUsername(USERNAME),
        new UserName("John Arrieta"),
        new UserEmail(EMAIL),
        UserPassword.fromPlainText(PASSWORD),
        UserRole.MEMBER,
        UserStatus.ACTIVE);
  }
}
