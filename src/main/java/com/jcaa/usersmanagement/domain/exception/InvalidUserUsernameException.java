package com.jcaa.usersmanagement.domain.exception;

// Errores de validación del username (nombre de usuario para iniciar sesión).
public final class InvalidUserUsernameException extends DomainException {

  private static final String MESSAGE_EMPTY = "The username must not be empty.";
  private static final String MESSAGE_LENGTH =
      "The username must have between %d and %d characters.";
  private static final String MESSAGE_FORMAT =
      "The username '%s' may only contain letters, numbers, dots, hyphens and underscores.";

  private InvalidUserUsernameException(final String message) {
    super(message);
  }

  public static InvalidUserUsernameException becauseValueIsEmpty() {
    return new InvalidUserUsernameException(MESSAGE_EMPTY);
  }

  public static InvalidUserUsernameException becauseLengthIsInvalid(
      final int minimumLength, final int maximumLength) {
    return new InvalidUserUsernameException(
        String.format(MESSAGE_LENGTH, minimumLength, maximumLength));
  }

  public static InvalidUserUsernameException becauseFormatIsInvalid(final String value) {
    return new InvalidUserUsernameException(String.format(MESSAGE_FORMAT, value));
  }
}
