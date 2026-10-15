package com.jcaa.usersmanagement.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GastoLugar")
class GastoLugarTest {

  @Test
  @DisplayName("elimina los espacios de los extremos")
  void shouldTrimValue() {
    // Act & Assert
    assertEquals("Exito San Diego", new GastoLugar("  Exito San Diego ").value());
  }

  @Test
  @DisplayName("rechaza un lugar vacío")
  void shouldRejectEmptyValue() {
    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoLugar("  "));
  }

  @Test
  @DisplayName("rechaza un lugar de más de 100 caracteres")
  void shouldRejectTooLongValue() {
    // Arrange
    final String tooLong = "a".repeat(GastoLugar.MAXIMUM_LENGTH + 1);

    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoLugar(tooLong));
  }
}
