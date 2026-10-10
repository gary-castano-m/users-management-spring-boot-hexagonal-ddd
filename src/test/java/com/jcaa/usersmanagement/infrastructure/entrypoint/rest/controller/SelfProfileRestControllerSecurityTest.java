package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.DeleteUserUseCase;
import com.jcaa.usersmanagement.application.port.in.GetUserByIdUseCase;
import com.jcaa.usersmanagement.application.port.in.UpdateUserUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserByIdQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests de seguridad para /api/users/me.
 *
 * <p>Levanta la configuración de seguridad real para comprobar que el perfil propio exige
 * autenticación y que un MEMBER puede consultarlo, aunque no pueda consultar a otros usuarios.
 */
@WebMvcTest(
    controllers = SelfProfileRestController.class,
    properties = "spring.main.web-application-type=servlet")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("SelfProfileRestController - seguridad")
class SelfProfileRestControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GetUserByIdUseCase getUserByIdUseCase;
  @MockBean private UpdateUserUseCase updateUserUseCase;
  @MockBean private DeleteUserUseCase deleteUserUseCase;
  @MockBean private JwtTokenService jwtTokenService;

  @Test
  @DisplayName("GET /api/users/me debe requerir autenticacion")
  void shouldRejectAnonymousAccess() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "u-own", roles = "MEMBER")
  @DisplayName("GET /api/users/me debe permitir a un MEMBER consultar su propio perfil")
  void shouldAllowMemberToReadOwnProfile() throws Exception {
    // Arrange
    when(getUserByIdUseCase.execute(new GetUserByIdQuery("u-own"))).thenReturn(member());

    // Act & Assert
    mockMvc
        .perform(get("/api/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("u-own"))
        .andExpect(jsonPath("$.role").value("MEMBER"));
  }

  private static UserModel member() {
    return new UserModel(
        new UserId("u-own"),
        new UserName("Ana Torres"),
        new UserEmail("ana@example.com"),
        UserPassword.fromPlainText("Pass1234"),
        UserRole.MEMBER,
        UserStatus.ACTIVE);
  }
}