package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.application.port.in.LoginUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.LoginCommand;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.LoginRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.LoginRestResponse;
import com.jcaa.usersmanagement.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

  private static final String EMAIL = "test@example.com";
  private static final String PASSWORD = "SecurePass1";

  @Mock private LoginUseCase loginUseCase;
  @Mock private JwtTokenService jwtTokenService;

  @Test
  void shouldAuthenticateAndReturnBearerToken() {
    // Arrange
    final UserModel user = activeUser();
    final LoginCommand command = new LoginCommand(EMAIL, PASSWORD);
    when(loginUseCase.execute(command)).thenReturn(user);
    when(jwtTokenService.generate(user)).thenReturn("signed.jwt.token");
    when(jwtTokenService.expirationSeconds()).thenReturn(900L);
    final AuthRestController controller = new AuthRestController(loginUseCase, jwtTokenService);

    // Act
    final LoginRestResponse response =
        controller.login(new LoginRestRequest(EMAIL, PASSWORD));

    // Assert
    assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(900L);
    verify(loginUseCase).execute(command);
  }

  @Test
  void shouldReturnAuthenticatedUserDataForTheClient() {
    // Arrange
    final UserModel user = activeUser();
    when(loginUseCase.execute(new LoginCommand(EMAIL, PASSWORD))).thenReturn(user);
    when(jwtTokenService.generate(user)).thenReturn("signed.jwt.token");
    when(jwtTokenService.expirationSeconds()).thenReturn(900L);
    final AuthRestController controller = new AuthRestController(loginUseCase, jwtTokenService);

    // Act
    final LoginRestResponse response =
        controller.login(new LoginRestRequest(EMAIL, PASSWORD));

    // Assert — el cliente necesita id, nombre y rol para decidir a qué pantalla ir
    assertThat(response.user().id()).isEqualTo("user-001");
    assertThat(response.user().name()).isEqualTo("Test User");
    assertThat(response.user().email()).isEqualTo(EMAIL);
    assertThat(response.user().role()).isEqualTo("ADMIN");
    assertThat(response.user().status()).isEqualTo("ACTIVE");
  }

  private static UserModel activeUser() {
    return new UserModel(
        new UserId("user-001"),
        new UserName("Test User"),
        new UserEmail(EMAIL),
        UserPassword.fromPlainText(PASSWORD),
        UserRole.ADMIN,
        UserStatus.ACTIVE);
  }
}
