package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

// ─────────────────────────────────────────────────────────────────────────────
// Respuesta del login. Además del token, incluye los datos del usuario
// autenticado para que los clientes (Kodular, Android) sepan a qué pantalla
// dirigirlo según su rol, sin tener que decodificar el JWT.
// Reutiliza UserRestResponse: un usuario se representa igual en toda la API.
// ─────────────────────────────────────────────────────────────────────────────
public record LoginRestResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UserRestResponse user) {}
