package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.util.Objects;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────────────────────
// Identidad de un gasto. Igual que UserId: la genera el dominio (UUID v4),
// no el cliente ni la base de datos.
// ─────────────────────────────────────────────────────────────────────────────
public record GastoId(String value) {

  public GastoId {
    final String normalizedValue = Objects.requireNonNull(value, "GastoId cannot be null").trim();
    if (normalizedValue.isEmpty()) {
      throw InvalidGastoException.becauseIdIsEmpty();
    }
    value = normalizedValue;
  }

  public static GastoId generate() {
    return new GastoId(UUID.randomUUID().toString());
  }

  @Override
  public String toString() {
    return value;
  }
}
