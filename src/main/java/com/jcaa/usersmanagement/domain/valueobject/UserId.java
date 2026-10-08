package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidUserIdException;
import java.util.Objects;
import java.util.UUID;

public record UserId(String value) {

  public UserId {
    final String normalizedValue = Objects.requireNonNull(value, "UserId cannot be null").trim();
    validateNotEmpty(normalizedValue);
    // asigna el valor normalizado al componente
    value = normalizedValue;
  }

     // ───────────────────────────────────────────────────────────────────────────
     // Fábrica de identidad: genera un identificador nuevo y único (UUID v4).
     // La identidad de un usuario es una regla del dominio; por eso se genera
     // aquí y no en el cliente (Kodular, Android) ni en la base de datos.
     // ───────────────────────────────────────────────────────────────────────────
     public static UserId generate() {
       return new UserId(UUID.randomUUID().toString());
     }
  private static void validateNotEmpty(final String normalizedValue) {
    if (normalizedValue.isEmpty()) {
      throw InvalidUserIdException.becauseValueIsEmpty();
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
