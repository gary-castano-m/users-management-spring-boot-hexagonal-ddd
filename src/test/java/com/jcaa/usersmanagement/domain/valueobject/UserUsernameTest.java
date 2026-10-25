package com.jcaa.usersmanagement.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidUserUsernameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UserUsername")
class UserUsernameTest {

  // --- Happy Path Tests ---

  @ParameterizedTest
  @ValueSource(strings = {"ana.torres", "  ana.torres  ", "Ana.Torres", "ANA.TORRES\t"})
  @DisplayName("Normaliza el username: sin espacios alrededor y en minúsculas")
  void shouldNormalizeUsername(final String username) {
    assertEquals("ana.torres", new UserUsername(username).value());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ana", "gary_c", "user-01", "a.b_c-9", "abcdefghijklmnopqrstuvwxyz1234"})
  @DisplayName("Acepta letras, números, punto, guion y guion bajo entre 3 y 30 caracteres")
  void shouldAcceptValidUsernames(final String username) {
    assertDoesNotThrow(() -> new UserUsername(username));
  }

  @Test
  @DisplayName("toString() devuelve el valor normalizado")
  void shouldReturnValueInToString() {
    assertEquals("gary", new UserUsername(" Gary ").toString());
  }

  @Test
  @DisplayName("Dos usernames que solo difieren en mayúsculas son iguales")
  void shouldBeEqualIgnoringCase() {
    assertEquals(new UserUsername("Gary"), new UserUsername("gary"));
  }

  // -- Flujo con excepciones y ramas de validación ---

  @Test
  @DisplayName("Rechaza un username nulo")
  void shouldRejectNull() {
    assertThrows(NullPointerException.class, () -> new UserUsername(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t"})
  @DisplayName("Rechaza un username vacío")
  void shouldRejectEmpty(final String username) {
    final InvalidUserUsernameException exception =
        assertThrows(InvalidUserUsernameException.class, () -> new UserUsername(username));
    assertTrue(exception.getMessage().contains("empty"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ab", " a ", "abcdefghijklmnopqrstuvwxyz12345"})
  @DisplayName("Rechaza un username con menos de 3 o más de 30 caracteres")
  void shouldRejectInvalidLength(final String username) {
    final InvalidUserUsernameException exception =
        assertThrows(InvalidUserUsernameException.class, () -> new UserUsername(username));
    assertTrue(exception.getMessage().contains("between 3 and 30"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ana torres", "ana@mail", "ana!", "añoro", "ana/torres"})
  @DisplayName("Rechaza espacios internos, símbolos no permitidos y letras con tilde o ñ")
  void shouldRejectInvalidCharacters(final String username) {
    assertThrows(InvalidUserUsernameException.class, () -> new UserUsername(username));
  }
}
