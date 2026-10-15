package com.jcaa.usersmanagement.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GastoId")
class GastoIdTest {

  @Test
  @DisplayName("generate() crea identificadores UUID distintos")
  void shouldGenerateDifferentUuids() {
    // Act
    final GastoId first = GastoId.generate();
    final GastoId second = GastoId.generate();

    // Assert
    assertAll(
        () -> assertDoesNotThrow(() -> UUID.fromString(first.value())),
        () -> assertNotEquals(first, second));
  }

  @Test
  @DisplayName("el constructor elimina los espacios de los extremos")
  void shouldTrimValue() {
    // Act & Assert
    assertEquals("g-1", new GastoId("  g-1  ").value());
  }

  @Test
  @DisplayName("el constructor rechaza un id vacío")
  void shouldRejectEmptyValue() {
    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoId("   "));
  }
}
