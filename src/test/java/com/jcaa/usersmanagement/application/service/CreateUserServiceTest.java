package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.GetUserByUsernamePort;
import com.jcaa.usersmanagement.application.port.out.SaveUserPort;
import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para CreateUserService.
 *
 * <p>Cubre: validación del command, email duplicado, username duplicado y flujo feliz (save +
 * notificación).
 */
@DisplayName("CreateUserService")
@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

  @Mock private SaveUserPort saveUserPort;
  @Mock private GetUserByEmailPort getUserByEmailPort;
  @Mock private GetUserByUsernamePort getUserByUsernamePort;
  @Mock private EmailNotificationService emailNotificationService;

  private CreateUserService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new CreateUserService(
              saveUserPort,
              getUserByEmailPort,
              getUserByUsernamePort,
              emailNotificationService,
              validatorFactory.getValidator());
    }
  }

  // ── flujo feliz

  @Test
  @DisplayName("execute() guarda el usuario y envía notificación cuando el email no existe")
  void shouldSaveUserAndNotifyWhenEmailIsNew() {
    // Arrange
    final CreateUserCommand command =
        new CreateUserCommand("u-01", "john.arrieta", "John Arrieta", "john@example.com", "Pass1234", "ADMIN");

    final UserModel savedUser =
        new UserModel(
            new UserId("u-01"),
            new UserUsername("john.arrieta"),
            new UserName("John Arrieta"),
            new UserEmail("john@example.com"),
            UserPassword.fromPlainText("Pass1234"),
            UserRole.ADMIN,
            UserStatus.PENDING);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());
    when(saveUserPort.save(any())).thenReturn(savedUser);

    // Act
    final UserModel result = service.execute(command);

    // Assert
    assertAll(
        "flujo feliz de CreateUserService",
        () -> assertNotNull(result, "resultado no debe ser null"),
        () -> assertEquals("u-01", result.getId().value(), "id del usuario guardado"));

    verify(saveUserPort).save(any(UserModel.class));
    verify(emailNotificationService).notifyUserCreated(savedUser, "Pass1234");
  }

  // ── email duplicado

  @Test
  @DisplayName("execute() lanza UserAlreadyExistsException cuando el email ya está registrado")
  void shouldThrowWhenEmailAlreadyExists() {
    // Arrange
    final CreateUserCommand command =
        new CreateUserCommand("u-02", "jane.doe", "Jane Doe", "jane@example.com", "Pass5678", "MEMBER");

    final UserModel existing =
        new UserModel(
            new UserId("u-99"),
            new UserUsername("jane.doe"),
            new UserName("Jane Doe"),
            new UserEmail("jane@example.com"),
            UserPassword.fromPlainText("OtraPass1"),
            UserRole.MEMBER,
            UserStatus.ACTIVE);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.of(existing));

    // Act & Assert
    assertThrows(UserAlreadyExistsException.class, () -> service.execute(command));
    verify(saveUserPort, never()).save(any());
    verify(emailNotificationService, never()).notifyUserCreated(any(), any());
  }

  // ── validación del command

  @Test
  @DisplayName(
      "execute() lanza ConstraintViolationException cuando el command tiene campos inválidos")
  void shouldThrowWhenCommandIsInvalid() {
    // Arrange — id en blanco y email inválido
    final CreateUserCommand command =
        new CreateUserCommand("", "usuario.test", "Jo", "not-an-email", "short", "ADMIN");

    // Act & Assert
    assertThrows(ConstraintViolationException.class, () -> service.execute(command));
    verifyNoInteractions(
        saveUserPort, getUserByEmailPort, getUserByUsernamePort, emailNotificationService);
  }

     // ── estado inicial del usuario

     @Test
     @DisplayName("execute() guarda al usuario en estado ACTIVE")
     void shouldSaveUserAsActive() {
       // Arrange
       final CreateUserCommand command =
           new CreateUserCommand("u-03", "ana.torres", "Ana Torres", "ana@example.com", "Pass1234", "MEMBER");
       final ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
       when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());
       when(saveUserPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

       // Act
       service.execute(command);

       // Assert
       verify(saveUserPort).save(captor.capture());
       assertEquals(UserStatus.ACTIVE, captor.getValue().getStatus());
     }

  // ── username duplicado

  @Test
  @DisplayName("execute() lanza UserAlreadyExistsException cuando el username ya está registrado")
  void shouldThrowWhenUsernameAlreadyExists() {
    // Arrange — el correo es nuevo, pero el username ya lo usa otra persona
    final CreateUserCommand command =
        new CreateUserCommand(
            "u-04", "jane.doe", "Jane Doe", "jane.nueva@example.com", "Pass5678", "MEMBER");

    final UserModel existing =
        new UserModel(
            new UserId("u-99"),
            new UserUsername("jane.doe"),
            new UserName("Jane Doe"),
            new UserEmail("jane@example.com"),
            UserPassword.fromHash("$2a$12$abcdefghijklmnopqrstuO"),
            UserRole.MEMBER,
            UserStatus.ACTIVE);

    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());
    when(getUserByUsernamePort.getByUsername(new UserUsername("jane.doe")))
        .thenReturn(Optional.of(existing));

    // Act
    final UserAlreadyExistsException exception =
        assertThrows(UserAlreadyExistsException.class, () -> service.execute(command));

    // Assert
    assertTrue(exception.getMessage().contains("jane.doe"), "el mensaje debe nombrar el username");
    verify(saveUserPort, never()).save(any());
    verify(emailNotificationService, never()).notifyUserCreated(any(), any());
  }

  // ── username normalizado

  @Test
  @DisplayName("execute() guarda el username en minúsculas y sin espacios alrededor")
  void shouldSaveNormalizedUsername() {
    // Arrange
    final CreateUserCommand command =
        new CreateUserCommand(
            "u-05", "  Ana.Torres  ", "Ana Torres", "ana@example.com", "Pass1234", "MEMBER");
    final ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());
    when(getUserByUsernamePort.getByUsername(any())).thenReturn(Optional.empty());
    when(saveUserPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    service.execute(command);

    // Assert
    verify(saveUserPort).save(captor.capture());
    assertEquals("ana.torres", captor.getValue().getUsername().value());
  }
}
