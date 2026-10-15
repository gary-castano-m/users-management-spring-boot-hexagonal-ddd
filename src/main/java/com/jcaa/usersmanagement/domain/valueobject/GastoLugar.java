package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.util.Objects;

// Lugar donde se realizó el gasto (ej.: "Éxito San Diego"). Obligatorio.
public record GastoLugar(String value) {

  public static final int MAXIMUM_LENGTH = 100;
  private static final String FIELD = "place";

  public GastoLugar {
    final String normalizedValue = Objects.requireNonNull(value, "GastoLugar cannot be null").trim();
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
