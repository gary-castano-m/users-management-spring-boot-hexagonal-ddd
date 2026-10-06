package com.jcaa.usersmanagement.infrastructure.adapter.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// ─────────────────────────────────────────────────────────────────────────────
// Adaptador de salida que envía correos mediante la API HTTP v3 de Brevo.
//
// Implementa el mismo puerto que JavaMailEmailSenderAdapter (EmailSenderPort),
// pero usa HTTPS (puerto 443) en lugar de SMTP. Es necesario en Render, cuyo
// plan gratuito bloquea los puertos SMTP (25, 465 y 587).
//
// Solo se activa cuando email.provider=brevo (variable EMAIL_PROVIDER).
// ─────────────────────────────────────────────────────────────────────────────
@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "brevo")
public class BrevoEmailSenderAdapter implements EmailSenderPort {

  // ── Encabezados y formato de la petición ─────────────────────────────────
  private static final String HEADER_API_KEY = "api-key";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_ACCEPT = "Accept";
  private static final String MEDIA_TYPE_JSON = "application/json";

  // ── Tiempos máximos de espera ────────────────────────────────────────────
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

  // ── Mensajes ─────────────────────────────────────────────────────────────
  private static final String LOG_SENT =
      "[BrevoEmailSenderAdapter] correo enviado exitosamente.";
  private static final String MSG_NOT_CONFIGURED =
      "Brevo no esta configurado: defina BREVO_API_KEY y BREVO_SENDER_EMAIL.";
  private static final String MSG_SEND_FAILED =
      "No se pudo enviar el correo a '%s' mediante Brevo. %s";

  private final BrevoConfig config;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  // ───────────────────────────────────────────────────────────────────────────
  // Constructor usado por Spring: crea un HttpClient real.
  // ───────────────────────────────────────────────────────────────────────────
  @Autowired
  public BrevoEmailSenderAdapter(final BrevoConfig config) {
    this(config, HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
  }

  // ───────────────────────────────────────────────────────────────────────────
  // Constructor para pruebas unitarias: permite inyectar un HttpClient simulado
  // y así probar el adaptador sin enviar correos reales.
  // ───────────────────────────────────────────────────────────────────────────
  BrevoEmailSenderAdapter(final BrevoConfig config, final HttpClient httpClient) {
    this.config = config;
    this.httpClient = httpClient;
  }

  @Override
  public void send(final EmailDestinationModel destination) {
    validateConfiguration();
    final HttpRequest request = buildRequest(destination);
    final HttpResponse<String> response = execute(request, destination);
    if (!isSuccessful(response.statusCode())) {
      throw failure(
          destination,
          "Brevo respondio HTTP " + response.statusCode() + ": " + response.body());
    }
    log.info(LOG_SENT);
  }

  // ───────────────────────────────────────────────────────────────────────────
  // Falla temprano y con un mensaje claro si faltan las variables de entorno,
  // en lugar de esperar un error 401 poco descriptivo de Brevo.
  // ───────────────────────────────────────────────────────────────────────────
  private void validateConfiguration() {
    if (isBlank(config.apiKey()) || isBlank(config.senderEmail())) {
      throw EmailSenderException.becauseSendFailed(new IllegalStateException(MSG_NOT_CONFIGURED));
    }
  }

  private HttpRequest buildRequest(final EmailDestinationModel destination) {
    return HttpRequest.newBuilder()
        .uri(URI.create(config.apiUrl()))
        .timeout(REQUEST_TIMEOUT)
        .header(HEADER_API_KEY, config.apiKey())
        .header(HEADER_CONTENT_TYPE, MEDIA_TYPE_JSON)
        .header(HEADER_ACCEPT, MEDIA_TYPE_JSON)
        .POST(HttpRequest.BodyPublishers.ofString(buildJsonBody(destination)))
        .build();
  }

  // ───────────────────────────────────────────────────────────────────────────
  // Traduce el modelo del dominio al formato JSON que espera Brevo:
  // { "sender": {...}, "to": [{...}], "subject": "...", "htmlContent": "..." }
  // ───────────────────────────────────────────────────────────────────────────
  private String buildJsonBody(final EmailDestinationModel destination) {
    final Map<String, Object> payload =
        Map.of(
            "sender", Map.of("name", config.senderName(), "email", config.senderEmail()),
            "to", List.of(
                Map.of(
                    "email", destination.getDestinationEmail(),
                    "name", destination.getDestinationName())),
            "subject", destination.getSubject(),
            "htmlContent", destination.getBody());
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (final JsonProcessingException exception) {
      throw failure(destination, "No se pudo generar el JSON: " + exception.getMessage());
    }
  }

  private HttpResponse<String> execute(
      final HttpRequest request, final EmailDestinationModel destination) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (final IOException exception) {
      throw failure(destination, "Error de red: " + exception.getMessage());
    } catch (final InterruptedException exception) {
      // Restaura la marca de interrupción del hilo antes de propagar el error.
      Thread.currentThread().interrupt();
      throw failure(destination, "El envio fue interrumpido.");
    }
  }

  private static boolean isSuccessful(final int statusCode) {
    return statusCode >= 200 && statusCode < 300;
  }

  private static boolean isBlank(final String value) {
    return value == null || value.isBlank();
  }

  // ───────────────────────────────────────────────────────────────────────────
  // Todos los errores se convierten en EmailSenderException, la misma excepción
  // que lanza el adaptador SMTP. Así EmailNotificationService los maneja igual,
  // sin saber qué proveedor se está usando. No se modifica el dominio:
  // se reutiliza el método becauseSendFailed que ya existía.
  // ───────────────────────────────────────────────────────────────────────────
  private static EmailSenderException failure(
      final EmailDestinationModel destination, final String detail) {
    return EmailSenderException.becauseSendFailed(
        new IllegalStateException(
            String.format(MSG_SEND_FAILED, destination.getDestinationEmail(), detail)));
  }
}