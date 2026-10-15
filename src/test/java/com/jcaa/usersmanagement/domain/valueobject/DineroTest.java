package com.jcaa.usersmanagement.domain.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests para Dinero.
 *
 * <p>Verifica la escala de 2 decimales con redondeo comercial, el rechazo de valores negativos o
 * nulos y las operaciones de suma y porcentaje.
 */
@DisplayName("Dinero")
class DineroTest {

  @Test
  @DisplayName("normaliza a 2 decimales con redondeo HALF_UP")
  void shouldNormalizeToTwoDecimalsRoundingHalfUp() {
    // Act & Assert
    assertAll(
        () -> assertEquals(new BigDecimal("1.01"), Dinero.of("1.005").value()),
        () -> assertEquals(new BigDecimal("5.00"), Dinero.of("5").value()),
        () -> assertEquals(Dinero.of("5.00"), Dinero.of("5"), "5 y 5.00 son el mismo valor"));
  }

  @Test
  @DisplayName("rechaza valores negativos")
  void shouldRejectNegativeValue() {
    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> Dinero.of("-0.01"));
  }

  @Test
  @DisplayName("rechaza un valor nulo")
  void shouldRejectNullValue() {
    // Act & Assert
    assertThrows(InvalidGastoException.class, () -> new Dinero(null));
  }

  @Test
  @DisplayName("sumar() devuelve un nuevo Dinero con la suma exacta")
  void shouldAddExactly() {
    // Act & Assert
    assertEquals(Dinero.of("0.30"), Dinero.of("0.10").sumar(Dinero.of("0.20")));
  }

  @Test
  @DisplayName("porcentaje() calcula el 19 % con redondeo comercial")
  void shouldCalculatePercentage() {
    // Arrange
    final BigDecimal tasa = new BigDecimal("0.19");

    // Act & Assert
    assertAll(
        () -> assertEquals(Dinero.of("19000.00"), Dinero.of("100000").porcentaje(tasa)),
        () -> assertEquals(Dinero.of("1909.50"), Dinero.of("10050").porcentaje(tasa)),
        () -> assertEquals(Dinero.of("0.01"), Dinero.of("0.05").porcentaje(tasa)));
  }
}
