package com.jcaa.usersmanagement.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GastoDescripcion")
class GastoDescripcionTest {

  @Test
  @DisplayName("elimina los espacios de los extremos")
  void shouldTrimValue() {
    // Act & Assert
    assertEquals("Mercado de la semana", new GastoDescripcion(" Mercado de la semana ").value());
  }

  @Test
  @DisplayName("rechaza una descripción vacía")
  void shouldRejectEmptyValue() {
    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoDescripcion(""));
  }

  @Test
  @DisplayName("rechaza una descripción de más de 255 caracteres")
  void shouldRejectTooLongValue() {
    // Arrange
    final String tooLong = "a".repeat(GastoDescripcion.MAXIMUM_LENGTH + 1);

    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoDescripcion(tooLong));
  }
}
