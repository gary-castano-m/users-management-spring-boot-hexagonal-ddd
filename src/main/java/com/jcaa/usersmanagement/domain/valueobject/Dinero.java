package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// ─────────────────────────────────────────────────────────────────────────────
// Cantidad de dinero en pesos colombianos, con 2 decimales.
//
// Usa BigDecimal y no double: con double, 0.1 + 0.2 da 0.30000000000000004,
// y en dinero esos errores se acumulan en los reportes. BigDecimal calcula
// exacto y redondea como se le indique (HALF_UP = redondeo comercial).
// Nunca es negativo. Es inmutable: sumar() y porcentaje() devuelven otro Dinero.
// ─────────────────────────────────────────────────────────────────────────────
public record Dinero(BigDecimal value) {

  public static final int DECIMALES = 2;
  public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

  public Dinero {
    if (Objects.isNull(value)) {
      throw InvalidGastoException.becauseValueIsNull();
    }
    if (value.signum() < 0) {
      throw InvalidGastoException.becauseValueIsNegative();
    }
    value = value.setScale(DECIMALES, RoundingMode.HALF_UP);
  }

  public static Dinero of(final String value) {
    return new Dinero(new BigDecimal(value));
  }

  public Dinero sumar(final Dinero other) {
    return new Dinero(value.add(other.value));
  }

  /** Calcula un porcentaje de este valor. Ej.: tasa 0.19 → 19 %. */
  public Dinero porcentaje(final BigDecimal tasa) {
    return new Dinero(value.multiply(tasa));
  }

  @Override
  public String toString() {
    return value.toPlainString();
  }
}
