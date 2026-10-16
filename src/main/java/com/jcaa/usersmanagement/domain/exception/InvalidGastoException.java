package com.jcaa.usersmanagement.domain.exception;

// ─────────────────────────────────────────────────────────────────────────────
// Errores de validación de un gasto. Una sola excepción con varias fábricas
// (becauseXxx), siguiendo el mismo estilo de las excepciones de usuario.
// Al ser DomainException, la API la responde como 400 Bad Request.
// ─────────────────────────────────────────────────────────────────────────────
public final class InvalidGastoException extends DomainException {

  private static final String MESSAGE_ID_EMPTY = "The expense id must not be empty.";
  private static final String MESSAGE_VALUE_NULL = "The expense value must not be null.";
  private static final String MESSAGE_VALUE_NEGATIVE = "The expense value must not be negative.";
  private static final String MESSAGE_DATE_NULL = "The expense date must not be null.";
  private static final String MESSAGE_DATE_FUTURE = "The expense date must not be in the future.";
  private static final String MESSAGE_FIELD_EMPTY = "The expense %s must not be empty.";
  private static final String MESSAGE_FIELD_TOO_LONG =
      "The expense %s must have at most %d characters.";
  private static final String MESSAGE_INVALID_DATE_RANGE =
      "The start date must not be after the end date.";
  private static final String MESSAGE_INCONSISTENT_TOTALS =
      "The expense total must be equal to the value without VAT plus the VAT.";

  private InvalidGastoException(final String message) {
    super(message);
  }

  public static InvalidGastoException becauseIdIsEmpty() {
    return new InvalidGastoException(MESSAGE_ID_EMPTY);
  }

  public static InvalidGastoException becauseValueIsNull() {
    return new InvalidGastoException(MESSAGE_VALUE_NULL);
  }

  public static InvalidGastoException becauseValueIsNegative() {
    return new InvalidGastoException(MESSAGE_VALUE_NEGATIVE);
  }

  public static InvalidGastoException becauseDateIsNull() {
    return new InvalidGastoException(MESSAGE_DATE_NULL);
  }

  public static InvalidGastoException becauseDateIsInTheFuture() {
    return new InvalidGastoException(MESSAGE_DATE_FUTURE);
  }

  public static InvalidGastoException becauseFieldIsEmpty(final String field) {
    return new InvalidGastoException(String.format(MESSAGE_FIELD_EMPTY, field));
  }

  public static InvalidGastoException becauseFieldIsTooLong(
      final String field, final int maximumLength) {
    return new InvalidGastoException(String.format(MESSAGE_FIELD_TOO_LONG, field, maximumLength));
  }

  public static InvalidGastoException becauseTotalsAreInconsistent() {
    return new InvalidGastoException(MESSAGE_INCONSISTENT_TOTALS);
  }

  public static InvalidGastoException becauseDateRangeIsInvalid() {
    return new InvalidGastoException(MESSAGE_INVALID_DATE_RANGE);
  }
}
