package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.SearchUsersUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
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
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests para UserReportRestController.
 *
 * <p>Levanta la seguridad real: verifica que el ADMIN obtiene el reporte filtrado y que un
 * MEMBER no puede consultarlo.
 */
@WebMvcTest(
    controllers = UserReportRestController.class,
    properties = "spring.main.web-application-type=servlet")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("UserReportRestController")
class UserReportRestControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private SearchUsersUseCase searchUsersUseCase;
  @MockBean private JwtTokenService jwtTokenService;

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("GET /api/users/reports?role=MEMBER devuelve el reporte filtrado al ADMIN")
  void shouldReturnReportFilteredByRole() throws Exception {
    // Arrange
    when(searchUsersUseCase.execute(new SearchUsersQuery("MEMBER", null)))
        .thenReturn(List.of(member()));

    // Act & Assert
    mockMvc
        .perform(get("/api/users/reports").param("role", "MEMBER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].role").value("MEMBER"));
  }

  @Test
  @WithMockUser(roles = "MEMBER")
  @DisplayName("GET /api/users/reports debe rechazar a un MEMBER")
  void shouldRejectReportForMember() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/api/users/reports").param("name", "ana"))
        .andExpect(status().isForbidden());
  }

  private static UserModel member() {
    return new UserModel(
        new UserId("u-100"),
        new UserName("Ana Torres"),
        new UserEmail("ana@example.com"),
        UserPassword.fromPlainText("Pass1234"),
        UserRole.MEMBER,
        UserStatus.ACTIVE);
  }
}