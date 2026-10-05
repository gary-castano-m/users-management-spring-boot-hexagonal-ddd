package com.jcaa.usersmanagement.infrastructure.adapter.email;

// ─────────────────────────────────────────────────────────────────────────────
// Configuración del proveedor de correo Brevo (API HTTP v3).
// Es el equivalente de SmtpConfig para el adaptador BrevoEmailSenderAdapter.
// No depende de Spring: puede crearse con "new" en las pruebas unitarias.
// ─────────────────────────────────────────────────────────────────────────────
public record BrevoConfig(
    String apiKey,
    String apiUrl,
    String senderEmail,
    String senderName) {

  // ───────────────────────────────────────────────────────────────────────────
  // El toString() automático de un record imprime TODOS sus campos, incluida
  // la API key. Se sobrescribe para que, si este objeto llega a un log,
  // el secreto nunca quede expuesto.
  // ───────────────────────────────────────────────────────────────────────────
  @Override
  public String toString() {
    return "BrevoConfig[apiUrl=" + apiUrl
        + ", senderEmail=" + senderEmail
        + ", senderName=" + senderName
        + ", apiKey=****]";
  }
}