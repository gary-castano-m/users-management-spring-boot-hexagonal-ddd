package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

public record DatabaseConfig(
    String host, int port, String databaseName, String username, String password) {
  // ─────────────────────────────────────────────────────────────────────────────
  // URL JDBC para PostgreSQL: jdbc:postgresql://<host>:<puerto>/<base_de_datos>
  // sslmode=prefer → usa conexión cifrada si el servidor la ofrece (Neon en la
  // nube) y conexión sin cifrar si no la ofrece (PostgreSQL local en Docker).
  // ─────────────────────────────────────────────────────────────────────────────
  private static final String URL_TEMPLATE =
      "jdbc:postgresql://%s:%d/%s?sslmode=prefer";

  public String buildJdbcUrl() {
    return String.format(URL_TEMPLATE, host, port, databaseName);
  }
}
