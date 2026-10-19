package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.BuscarGastosUseCase;
import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.query.BuscarGastosQuery;
import com.jcaa.usersmanagement.application.service.dto.result.ReporteGastos;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.security.JwtAuthenticationFilter;
import com.jcaa.usersmanagement.infrastructure.security.JwtTokenService;
import com.jcaa.usersmanagement.infrastructure.security.RestAccessDeniedHandler;
import com.jcaa.usersmanagement.infrastructure.security.RestAuthenticationEntryPoint;
import com.jcaa.usersmanagement.infrastructure.security.SecurityConfig;
import java.time.LocalDate;
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
 * Tests para GastoReportRestController: parámetros del reporte, totales en la respuesta, fecha
 * con formato inválido (400) y acceso sin token (401).
 */
@WebMvcTest(
    controllers = GastoReportRestController.class,
    properties = "spring.main.web-application-type=servlet")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("GastoReportRestController")
class GastoReportRestControllerTest {

  private static final String GASTO_ID = "g-001";

  @Autowired private MockMvc mockMvc;

  @MockBean private BuscarGastosUseCase buscarGastosUseCase;
  @MockBean private JwtTokenService jwtTokenService;

  // Gasto de Ana: 100 sin IVA → 19 de IVA → 119 en total
  private static GastoDetalle gastoDeAna() {
    final GastoModel gasto =
        GastoModel.registrar(
            new GastoId(GASTO_ID),
            new UserId("u-ana"),
            new GastoFecha(LocalDate.of(2026, 10, 1)),
            Dinero.of("100"),
            new GastoLugar("Exito"),
            new GastoDescripcion("Mercado"));
    return new GastoDetalle(gasto, "Ana Torres");
  }

  @Test
  @WithMockUser(username = "u-adm", roles = "ADMIN")
  @DisplayName("GET /api/gastos/reports por fechas devuelve los totales y los gastos")
  void shouldReturnReportByDateRange() throws Exception {
    // Arrange
    when(buscarGastosUseCase.execute(any())).thenReturn(ReporteGastos.de(List.of(gastoDeAna())));

    // Act & Assert
    mockMvc
        .perform(get("/api/gastos/reports").param("desde", "2026-10-01").param("hasta", "2026-10-31"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cantidad").value(1))
        .andExpect(jsonPath("$.totalIva").value(19.0))
        .andExpect(jsonPath("$.totalConIva").value(119.0))
        .andExpect(jsonPath("$.gastos[0].id").value(GASTO_ID));
    verify(buscarGastosUseCase)
        .execute(
            new BuscarGastosQuery(
                new Solicitante(new UserId("u-adm"), UserRole.ADMIN),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                null));
  }

  @Test
  @WithMockUser(username = "u-adm", roles = "ADMIN")
  @DisplayName("GET /api/gastos/reports con una fecha inválida responde 400")
  void shouldRejectInvalidDate() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/api/gastos/reports").param("desde", "2026-13-45"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(buscarGastosUseCase);
  }

  @Test
  @DisplayName("GET /api/gastos/reports sin token responde 401")
  void shouldRejectAnonymousRequest() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api/gastos/reports").param("lugar", "exito"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(buscarGastosUseCase);
  }
}
