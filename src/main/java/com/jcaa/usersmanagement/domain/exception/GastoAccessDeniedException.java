package com.jcaa.usersmanagement.domain.exception;

// ─────────────────────────────────────────────────────────────────────────────
// El usuario puede VER el gasto, pero no modificarlo ni eliminarlo
// (por ejemplo, un REVIEWER frente al gasto de otra persona). La API lo
// responderá como 403 Forbidden.
// Si el usuario ni siquiera puede verlo, se usa GastoNotFoundException (404).
// ─────────────────────────────────────────────────────────────────────────────
public final class GastoAccessDeniedException extends DomainException {

  private static final String MESSAGE_MODIFY =
      "You are not allowed to modify the expense with id '%s'.";

  private GastoAccessDeniedException(final String message) {
    super(message);
  }

  public static GastoAccessDeniedException becauseCannotModify(final String gastoId) {
    return new GastoAccessDeniedException(String.format(MESSAGE_MODIFY, gastoId));
  }
}
