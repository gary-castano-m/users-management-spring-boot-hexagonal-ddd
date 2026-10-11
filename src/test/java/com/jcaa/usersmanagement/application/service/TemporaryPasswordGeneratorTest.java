package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests para TemporaryPasswordGenerator.
 *
 * <p>Verifica que la contraseña temporal cumple la longitud mínima del dominio, usa solo el
 * alfabeto permitido y no se repite entre llamadas.
 */
@DisplayName("TemporaryPasswordGenerator")
class TemporaryPasswordGeneratorTest {

  private final TemporaryPasswordGenerator generator = new TemporaryPasswordGenerator();

  @Test
  @DisplayName("generate() devuelve 12 caracteres del alfabeto permitido")
  void shouldGeneratePasswordWithExpectedLengthAndAlphabet() {
    // Act
    final String password = generator.generate();

    // Assert
    assertAll(
        "contraseña temporal",
        () -> assertEquals(TemporaryPasswordGenerator.LENGTH, password.length()),
        () ->
            assertTrue(
                password.chars().allMatch(c -> TemporaryPasswordGenerator.ALPHABET.indexOf(c) >= 0),
                "solo debe usar caracteres del alfabeto"));
  }

  @Test
  @DisplayName("generate() devuelve una contraseña distinta en cada llamada")
  void shouldGenerateDifferentPasswords() {
    // Act & Assert
    assertNotEquals(generator.generate(), generator.generate());
  }
}
