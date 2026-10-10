package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.application.port.in.DeleteUserUseCase;
import com.jcaa.usersmanagement.application.port.in.GetUserByIdUseCase;
import com.jcaa.usersmanagement.application.port.in.UpdateUserUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteUserCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateUserCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserByIdQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.UpdateProfileRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserRestResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Tests para SelfProfileRestController.
 *
 * <p>Verifica que todas las operaciones usan el id del token (no uno enviado por el cliente) y
 * que la edición del perfil conserva el rol y el estado del usuario.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SelfProfileRestController")
class SelfProfileRestControllerTest {

  private static final String OWN_ID = "u-own";

  @Mock private GetUserByIdUseCase getUserByIdUseCase;
  @Mock private UpdateUserUseCase updateUserUseCase;
  @Mock private DeleteUserUseCase deleteUserUseCase;

  private SelfProfileRestController controller;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    controller =
        new SelfProfileRestController(getUserByIdUseCase, updateUserUseCase, deleteUserUseCase);
    // Simula la autenticación que crea el filtro JWT: el "nombre" es el id del token
    authentication = new UsernamePasswordAuthenticationToken(OWN_ID, null, List.of());
  }

  @Test
  @DisplayName("getProfile() consulta al usuario identificado por el token")
  void shouldReturnProfileOfAuthenticatedUser() {
    // Arrange
    when(getUserByIdUseCase.execute(new GetUserByIdQuery(OWN_ID))).thenReturn(member());

    // Act
    final UserRestResponse response = controller.getProfile(authentication);

    // Assert
    assertEquals(OWN_ID, response.id());
  }

  @Test
  @DisplayName("updateProfile() usa el id del token y conserva el rol y el estado actuales")
  void shouldKeepRoleAndStatusWhenUpdatingOwnProfile() {
    // Arrange
    final ArgumentCaptor<UpdateUserCommand> captor =
        ArgumentCaptor.forClass(UpdateUserCommand.class);
    when(getUserByIdUseCase.execute(new GetUserByIdQuery(OWN_ID))).thenReturn(member());
    when(updateUserUseCase.execute(captor.capture())).thenReturn(member());
    final UpdateProfileRestRequest request =
        new UpdateProfileRestRequest("Ana Maria Torres", "ana.maria@example.com", null);

    // Act
    controller.updateProfile(authentication, request);

    // Assert
    final UpdateUserCommand command = captor.getValue();
    assertAll(
        "la edicion del perfil no debe permitir cambiar id, rol ni estado",
        () -> assertEquals(OWN_ID, command.id(), "el id sale del token"),
        () -> assertEquals("MEMBER", command.role(), "el rol se conserva"),
        () -> assertEquals("ACTIVE", command.status(), "el estado se conserva"),
        () -> assertEquals("Ana Maria Torres", command.name()),
        () -> assertEquals("ana.maria@example.com", command.email()));
  }

  @Test
  @DisplayName("deleteProfile() elimina al usuario identificado por el token")
  void shouldDeleteAuthenticatedUser() {
    // Act
    controller.deleteProfile(authentication);

    // Assert
    verify(deleteUserUseCase).execute(new DeleteUserCommand(OWN_ID));
  }

  private static UserModel member() {
    return new UserModel(
        new UserId(OWN_ID),
        new UserName("Ana Torres"),
        new UserEmail("ana@example.com"),
        UserPassword.fromPlainText("Pass1234"),
        UserRole.MEMBER,
        UserStatus.ACTIVE);
  }
}