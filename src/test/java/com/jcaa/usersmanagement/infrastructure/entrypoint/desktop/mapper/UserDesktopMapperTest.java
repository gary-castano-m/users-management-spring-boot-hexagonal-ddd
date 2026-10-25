package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateUserCommand;
import com.jcaa.usersmanagement.domain.valueobject.UserUsername;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.CreateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UpdateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests para UserDesktopMapper.
 *
 * <p>La consola de escritorio no pide username: se deriva del correo. Se prueba la regla de
 * derivación y que los comandos de crear/actualizar la usen.
 */
@DisplayName("UserDesktopMapper")
class UserDesktopMapperTest {

  @ParameterizedTest
  @CsvSource({
    "ana@example.com, ana",
    "Ana.Perez@example.com, ana.perez",
    "ana+test@example.com, ana_test",
    "jo@example.com, jo_",
    "  luis_m@example.com  , luis_m"
  })
  @DisplayName("usernameFromEmail() toma la parte antes de la @ y la vuelve un username válido")
  void shouldDeriveUsernameFromEmail(final String email, final String expected) {
    final String username = UserDesktopMapper.usernameFromEmail(email);
    assertEquals(expected, username);
    assertDoesNotThrow(() -> new UserUsername(username), "debe ser un username válido");
  }

  @Test
  @DisplayName("usernameFromEmail() recorta a 30 caracteres")
  void shouldTruncateLongUsernames() {
    final String email = "a".repeat(40) + "@example.com";
    assertEquals(30, UserDesktopMapper.usernameFromEmail(email).length());
  }

  @Test
  @DisplayName("usernameFromEmail() devuelve null si el correo es null (lo rechaza la validación)")
  void shouldReturnNullForNullEmail() {
    assertNull(UserDesktopMapper.usernameFromEmail(null));
  }

  @Test
  @DisplayName("toCreateCommand() envía el username derivado del correo")
  void shouldUseDerivedUsernameWhenCreating() {
    final CreateUserCommand command =
        UserDesktopMapper.toCreateCommand(
            new CreateUserRequest("u-1", "Ana Torres", "ana@example.com", "Pass1234", "MEMBER"));
    assertEquals("ana", command.username());
  }

  @Test
  @DisplayName("toUpdateCommand() envía el username derivado del correo")
  void shouldUseDerivedUsernameWhenUpdating() {
    final UpdateUserCommand command =
        UserDesktopMapper.toUpdateCommand(
            new UpdateUserRequest(
                "u-1", "Ana Torres", "ana.t@example.com", null, "MEMBER", "ACTIVE"));
    assertEquals("ana.t", command.username());
  }
}
