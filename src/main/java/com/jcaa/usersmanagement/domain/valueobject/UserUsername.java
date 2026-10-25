package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidUserUsernameException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

// ─────────────────────────────────────────────────────────────────────────────
// Nombre de usuario con el que se inicia sesión (además del correo).
// Reglas: de 3 a 30 caracteres; solo letras, números, punto, guion y guion
// bajo. Se guarda en minúsculas para que "Ana" y "ana" sean el mismo usuario.
// No confundir con UserName, que es el nombre de la persona.
// ─────────────────────────────────────────────────────────────────────────────
public record UserUsername(String value) {

  public static final int MINIMUM_LENGTH = 3;
  public static final int MAXIMUM_LENGTH = 30;
  private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[a-z0-9._-]+$");

  public UserUsername {
    final String normalizedValue =
        Objects.requireNonNull(value, "UserUsername cannot be null").trim().toLowerCase(Locale.ROOT);
    validateNotEmpty(normalizedValue);
    validateLength(normalizedValue);
    validateFormat(normalizedValue);
    value = normalizedValue;
  }

  private static void validateNotEmpty(final String normalizedValue) {
    if (normalizedValue.isEmpty()) {
      throw InvalidUserUsernameException.becauseValueIsEmpty();
    }
  }

  private static void validateLength(final String normalizedValue) {
    if (normalizedValue.length() < MINIMUM_LENGTH || normalizedValue.length() > MAXIMUM_LENGTH) {
      throw InvalidUserUsernameException.becauseLengthIsInvalid(MINIMUM_LENGTH, MAXIMUM_LENGTH);
    }
  }

  private static void validateFormat(final String normalizedValue) {
    if (!ALLOWED_CHARACTERS.matcher(normalizedValue).matches()) {
      throw InvalidUserUsernameException.becauseFormatIsInvalid(normalizedValue);
    }
  }

  @Override
  public String toString() {
    return value;
  }
}
