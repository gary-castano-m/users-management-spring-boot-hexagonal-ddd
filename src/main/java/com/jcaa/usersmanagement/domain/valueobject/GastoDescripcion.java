package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.util.Objects;

// Descripción del gasto (ej.: "Mercado de la semana"). Obligatoria.
public record GastoDescripcion(String value) {

  public static final int MAXIMUM_LENGTH = 255;
  private static final String FIELD = "description";

  public GastoDescripcion {
    final String normalizedValue =
        Objects.requireNonNull(value, "GastoDescripcion cannot be null").trim();
    if (normalizedValue.isEmpty()) {
      throw InvalidGastoException.becauseFieldIsEmpty(FIELD);
    }
    if (normalizedValue.length() > MAXIMUM_LENGTH) {
      throw InvalidGastoException.becauseFieldIsTooLong(FIELD, MAXIMUM_LENGTH);
    }
    value = normalizedValue;
  }

  @Override
  public String toString() {
    return value;
  }
}
