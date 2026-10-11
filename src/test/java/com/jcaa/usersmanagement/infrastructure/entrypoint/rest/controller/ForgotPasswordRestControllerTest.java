package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.ResetPasswordUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.ResetPasswordCommand;
import com.jcaa.usersmanagement.infrastructure.security.JwtAuthenticationFilter;
import com.jcaa.usersmanagement.infrastructure.security.JwtTokenService;
import com.jcaa.usersmanagement.infrastructure.security.RestAccessDeniedHandler;
import com.jcaa.usersmanagement.infrastructure.security.RestAuthenticationEntryPoint;
import com.jcaa.usersmanagement.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests para ForgotPasswordRestController, con la configuración de seguridad real.
 *
 * <p>Verifica que el endpoint es público (sin token), que responde 204 y que rechaza un correo
 * inválido con 400 sin invocar el caso de uso.
 */
@WebMvcTest(
    controllers = ForgotPasswordRestController.class,
    properties = "spring.main.web-application-type=servlet")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("ForgotPasswordRestController")
class ForgotPasswordRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ResetPasswordUseCase resetPasswordUseCase;
  @MockBean private JwtTokenService jwtTokenService;

  @Test
  @DisplayName("POST /api/auth/forgot-password es público y responde 204")
  void shouldAcceptAnonymousRequestWithNoContent() throws Exception {
    // Act
    mockMvc
        .perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\"}"))
        .andExpect(status().isNoContent());

    // Assert
    verify(resetPasswordUseCase).execute(new ResetPasswordCommand("ana@example.com"));
  }

  @Test
  @DisplayName("POST /api/auth/forgot-password rechaza un correo inválido con 400")
  void shouldRejectInvalidEmail() throws Exception {
    // Act
    mockMvc
        .perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-es-un-correo\"}"))
        .andExpect(status().isBadRequest());

    // Assert
    verify(resetPasswordUseCase, never()).execute(any());
  }
}
