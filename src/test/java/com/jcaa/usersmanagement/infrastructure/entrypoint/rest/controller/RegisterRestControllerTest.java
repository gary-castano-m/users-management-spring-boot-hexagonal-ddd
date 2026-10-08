package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.application.port.in.CreateUserUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.RegisterRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserRestResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para RegisterRestController.
 *
 * <p>Verifica que el registro público crea siempre un usuario MEMBER, con un id generado por el
 * servidor, y que la respuesta refleja al usuario activo.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterRestController")
class RegisterRestControllerTest {

  @Mock private CreateUserUseCase createUserUseCase;

  @Test
  @DisplayName("register() crea un usuario MEMBER con id generado por el servidor")
  void shouldRegisterMemberWithGeneratedId() {
    // Arrange
    final ArgumentCaptor<CreateUserCommand> captor =
        ArgumentCaptor.forClass(CreateUserCommand.class);
    final UserModel created =
        new UserModel(
            new UserId("u-100"),
            new UserName("Ana Torres"),
            new UserEmail("ana@example.com"),
            UserPassword.fromPlainText("Pass1234"),
            UserRole.MEMBER,
            UserStatus.ACTIVE);
    when(createUserUseCase.execute(captor.capture())).thenReturn(created);
    final RegisterRestController controller = new RegisterRestController(createUserUseCase);

    // Act
    final UserRestResponse response =
        controller.register(new RegisterRestRequest("Ana Torres", "ana@example.com", "Pass1234"));

    // Assert
    final CreateUserCommand command = captor.getValue();
    assertAll(
        "el registro publico debe crear un MEMBER activo con id del servidor",
        () -> assertEquals("MEMBER", command.role(), "el rol siempre es MEMBER"),
        () -> assertDoesNotThrow(() -> UUID.fromString(command.id()), "id generado (UUID)"),
        () -> assertEquals("ana@example.com", command.email()),
        () -> assertEquals("ACTIVE", response.status()));
  }
}