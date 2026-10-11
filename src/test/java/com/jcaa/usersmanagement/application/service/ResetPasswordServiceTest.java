package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.port.out.GetUserByEmailPort;
import com.jcaa.usersmanagement.application.port.out.UpdateUserPort;
import com.jcaa.usersmanagement.application.service.dto.command.ResetPasswordCommand;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
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
 * Tests para ResetPasswordService.
 *
 * <p>Verifica que un usuario activo recibe una contraseña temporal (guardada como hash) y que, si
 * el correo no existe o el usuario no está activo, no se modifica nada ni se envía correo.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResetPasswordService")
class ResetPasswordServiceTest {

  private static final String EMAIL = "ana@example.com";
  private static final String OLD_PASSWORD = "Pass1234";
  private static final String TEMPORARY_PASSWORD = "Tmp7kQ2xR9aB";

  @Mock private GetUserByEmailPort getUserByEmailPort;
  @Mock private UpdateUserPort updateUserPort;
  @Mock private TemporaryPasswordGenerator temporaryPasswordGenerator;
  @Mock private EmailNotificationService emailNotificationService;

  private ResetPasswordService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new ResetPasswordService(
              getUserByEmailPort,
              updateUserPort,
              temporaryPasswordGenerator,
              emailNotificationService,
              validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("execute() guarda el hash de la contraseña temporal y la envía por correo")
  void shouldResetPasswordAndNotifyActiveUser() {
    // Arrange
    when(getUserByEmailPort.getByEmail(new UserEmail(EMAIL)))
        .thenReturn(Optional.of(user(UserStatus.ACTIVE)));
    when(temporaryPasswordGenerator.generate()).thenReturn(TEMPORARY_PASSWORD);
    when(updateUserPort.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    final ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);

    // Act
    service.execute(new ResetPasswordCommand(EMAIL));

    // Assert
    verify(updateUserPort).update(captor.capture());
    final UserModel saved = captor.getValue();
    assertAll(
        "la contraseña guardada debe ser la temporal, cifrada",
        () -> assertTrue(saved.getPassword().verifyPlain(TEMPORARY_PASSWORD)),
        () -> assertFalse(saved.getPassword().verifyPlain(OLD_PASSWORD)),
        () -> assertNotEquals(TEMPORARY_PASSWORD, saved.getPassword().value(), "nunca en claro"),
        () -> assertEquals(UserStatus.ACTIVE, saved.getStatus()));
    verify(emailNotificationService).notifyPasswordReset(saved, TEMPORARY_PASSWORD);
  }

  @Test
  @DisplayName("execute() no hace nada si el correo no está registrado")
  void shouldDoNothingWhenEmailIsUnknown() {
    // Arrange
    when(getUserByEmailPort.getByEmail(any())).thenReturn(Optional.empty());

    // Act
    assertDoesNotThrow(() -> service.execute(new ResetPasswordCommand("nadie@example.com")));

    // Assert
    verifyNoInteractions(updateUserPort, temporaryPasswordGenerator, emailNotificationService);
  }

  @Test
  @DisplayName("execute() no hace nada si el usuario está inactivo")
  void shouldDoNothingWhenUserIsInactive() {
    // Arrange
    when(getUserByEmailPort.getByEmail(any()))
        .thenReturn(Optional.of(user(UserStatus.INACTIVE)));

    // Act
    service.execute(new ResetPasswordCommand(EMAIL));

    // Assert
    verifyNoInteractions(updateUserPort, temporaryPasswordGenerator, emailNotificationService);
  }

  @Test
  @DisplayName("execute() rechaza un correo con formato inválido")
  void shouldRejectInvalidEmail() {
    // Act & Assert
    assertThrows(
        ConstraintViolationException.class,
        () -> service.execute(new ResetPasswordCommand("no-es-un-correo")));
    verifyNoInteractions(getUserByEmailPort);
  }

  private static UserModel user(final UserStatus status) {
    return new UserModel(
        new UserId("u-100"),
        new UserName("Ana Torres"),
        new UserEmail(EMAIL),
        UserPassword.fromPlainText(OLD_PASSWORD),
        UserRole.MEMBER,
        status);
  }
}
