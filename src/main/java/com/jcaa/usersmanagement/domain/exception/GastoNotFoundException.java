package com.jcaa.usersmanagement.domain.exception;

// ─────────────────────────────────────────────────────────────────────────────
// El gasto no existe, o existe pero pertenece a otro usuario.
// En ambos casos se responde igual (404): así nadie puede averiguar si un id
// de gasto ajeno existe (protección contra enumeración).
// ─────────────────────────────────────────────────────────────────────────────
public final class GastoNotFoundException extends DomainException {

  private static final String MESSAGE_BY_ID = "The expense with id '%s' was not found.";

  private GastoNotFoundException(final String message) {
    super(message);
  }

  public static GastoNotFoundException becauseIdWasNotFound(final String gastoId) {
    return new GastoNotFoundException(String.format(MESSAGE_BY_ID, gastoId));
  }
}
