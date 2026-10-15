package com.jcaa.usersmanagement.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GastoFecha")
class GastoFechaTest {

  @Test
  @DisplayName("acepta la fecha de hoy (hora de Colombia)")
  void shouldAcceptToday() {
    // Arrange
    final LocalDate today = LocalDate.now(GastoFecha.ZONA_COLOMBIA);

    // Act & Assert
    assertEquals(today, new GastoFecha(today).value());
  }

  @Test
  @DisplayName("rechaza una fecha futura")
  void shouldRejectFutureDate() {
    // Arrange
    final LocalDate tomorrow = LocalDate.now(GastoFecha.ZONA_COLOMBIA).plusDays(1);

    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoFecha(tomorrow));
  }

  @Test
  @DisplayName("rechaza una fecha nula")
  void shouldRejectNullDate() {
    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new GastoFecha(null));
  }
}
