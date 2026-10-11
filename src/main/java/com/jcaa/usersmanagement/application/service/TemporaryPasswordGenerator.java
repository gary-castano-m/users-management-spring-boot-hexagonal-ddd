package com.jcaa.usersmanagement.application.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

// ─────────────────────────────────────────────────────────────────────────────
// Genera contraseñas temporales para la recuperación de contraseña.
// Usa SecureRandom (criptográficamente seguro), no Random, para que la
// contraseña no se pueda predecir. Se omiten caracteres que se confunden al
// leerlos en un correo (0/O, 1/l/I).
// ─────────────────────────────────────────────────────────────────────────────
@Component
public class TemporaryPasswordGenerator {

  static final int LENGTH = 12;
  static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

  private final SecureRandom random = new SecureRandom();

  public String generate() {
    final StringBuilder password = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      password.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return password.toString();
  }
}
