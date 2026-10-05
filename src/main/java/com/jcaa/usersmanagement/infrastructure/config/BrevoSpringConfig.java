package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ─────────────────────────────────────────────────────────────────────────────
// Lee las propiedades brevo.* y crea el bean BrevoConfig.
// Sigue el mismo patrón que SmtpSpringConfig.
// ─────────────────────────────────────────────────────────────────────────────
@Configuration
public class BrevoSpringConfig {

  // ───────────────────────────────────────────────────────────────────────────
  // Cada propiedad lleva un valor por defecto (después de ":") para que el
  // contexto de Spring arranque aunque la propiedad no exista; por ejemplo,
  // con el application.properties de pruebas, que no define nada de Brevo.
  // ───────────────────────────────────────────────────────────────────────────
  private static final String PROP_API_KEY      = "${brevo.api-key:}";
  private static final String PROP_API_URL      = "${brevo.api-url:https://api.brevo.com/v3/smtp/email}";
  private static final String PROP_SENDER_EMAIL = "${brevo.sender.email:}";
  private static final String PROP_SENDER_NAME  = "${brevo.sender.name:Oye! - Organiza tus gastos}";

  @Value(PROP_API_KEY)
  private String apiKey;

  @Value(PROP_API_URL)
  private String apiUrl;

  @Value(PROP_SENDER_EMAIL)
  private String senderEmail;

  @Value(PROP_SENDER_NAME)
  private String senderName;

  @Bean
  public BrevoConfig brevoConfig() {
    return new BrevoConfig(apiKey, apiUrl, senderEmail, senderName);
  }
}