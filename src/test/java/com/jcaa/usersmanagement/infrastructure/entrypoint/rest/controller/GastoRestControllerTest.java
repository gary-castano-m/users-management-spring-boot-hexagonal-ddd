package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.usersmanagement.application.port.in.ActualizarGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.BuscarGastosUseCase;
import com.jcaa.usersmanagement.application.port.in.ConsultarGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.EliminarGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.RegistrarGastoUseCase;
import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.command.EliminarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.command.RegistrarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.ReporteGastos;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.exception.GastoAccessDeniedException;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests para GastoRestController, con la configuración de seguridad real.
 *
 * <p>Verifica que /api/gastos exige autenticación, que el dueño sale del token (no del cuerpo),
 * la validación del cuerpo y la traducción de errores: 404 si el gasto no es visible y 403 si se
 * ve pero no se puede modificar.
 */
@WebMvcTest(
    controllers = GastoRestController.class,
    properties = "spring.main.web-application-type=servlet")
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  RestAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class
})
@DisplayName("GastoRestController")
class GastoRestControllerTest {

  private static final String GASTO_ID = "g-001";
  private static final String BODY =
      "{\"fecha\":\"2026-10-01\",\"valorTotalSinIva\":100,"
          + "\"lugar\":\"Exito\",\"descripcion\":\"Mercado\"}";

  @Autowired private MockMvc mockMvc;

  @MockBean private RegistrarGastoUseCase registrarGastoUseCase;
  @MockBean private ActualizarGastoUseCase actualizarGastoUseCase;
  @MockBean private EliminarGastoUseCase eliminarGastoUseCase;
  @MockBean private ConsultarGastoUseCase consultarGastoUseCase;
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
  @DisplayName("POST /api/gastos sin token responde 401")
  void shouldRejectAnonymousRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(registrarGastoUseCase);
  }

  @Test
  @WithMockUser(username = "u-ana", roles = "MEMBER")
  @DisplayName("POST /api/gastos registra a nombre del usuario del token y devuelve el IVA")
  void shouldRegisterGastoForTokenUser() throws Exception {
    // Arrange
    when(registrarGastoUseCase.execute(any())).thenReturn(gastoDeAna());
    final ArgumentCaptor<RegistrarGastoCommand> captor =
        ArgumentCaptor.forClass(RegistrarGastoCommand.class);

    // Act & Assert
    mockMvc
        .perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(GASTO_ID))
        .andExpect(jsonPath("$.fecha").value("2026-10-01"))
        .andExpect(jsonPath("$.ivaTotal").value(19.0))
        .andExpect(jsonPath("$.valorTotalConIva").value(119.0))
        .andExpect(jsonPath("$.nombreUsuario").value("Ana Torres"));
    verify(registrarGastoUseCase).execute(captor.capture());
    assertEquals(
        new Solicitante(new UserId("u-ana"), UserRole.MEMBER), captor.getValue().solicitante());
    assertEquals(new BigDecimal("100"), captor.getValue().valorTotalSinIva());
  }

  @Test
  @WithMockUser(username = "u-ana", roles = "MEMBER")
  @DisplayName("POST /api/gastos sin lugar responde 400 y no registra nada")
  void shouldRejectInvalidBody() throws Exception {
    // Arrange
    final String sinLugar =
        "{\"fecha\":\"2026-10-01\",\"valorTotalSinIva\":100,\"descripcion\":\"Mercado\"}";

    // Act & Assert
    mockMvc
        .perform(post("/api/gastos").contentType(MediaType.APPLICATION_JSON).content(sinLugar))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(registrarGastoUseCase);
  }

  @Test
  @WithMockUser(username = "u-ana", roles = "MEMBER")
  @DisplayName("GET /api/gastos devuelve el listado del usuario")
  void shouldListGastos() throws Exception {
    // Arrange
    when(buscarGastosUseCase.execute(any())).thenReturn(ReporteGastos.de(List.of(gastoDeAna())));

    // Act & Assert
    mockMvc
        .perform(get("/api/gastos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(GASTO_ID))
        .andExpect(jsonPath("$[0].nombreUsuario").value("Ana Torres"));
  }

  @Test
  @WithMockUser(username = "u-luis", roles = "MEMBER")
  @DisplayName("GET /api/gastos/{id} de un gasto no visible responde 404")
  void shouldReturnNotFoundForHiddenGasto() throws Exception {
    // Arrange
    when(consultarGastoUseCase.execute(any()))
        .thenThrow(GastoNotFoundException.becauseIdWasNotFound(GASTO_ID));

    // Act & Assert
    mockMvc.perform(get("/api/gastos/{id}", GASTO_ID)).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "u-rev", roles = "REVIEWER")
  @DisplayName("PUT /api/gastos/{id} sin permiso de modificación responde 403")
  void shouldReturnForbiddenWhenCannotModify() throws Exception {
    // Arrange
    when(actualizarGastoUseCase.execute(any()))
        .thenThrow(GastoAccessDeniedException.becauseCannotModify(GASTO_ID));

    // Act & Assert
    mockMvc
        .perform(
            put("/api/gastos/{id}", GASTO_ID).contentType(MediaType.APPLICATION_JSON).content(BODY))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "u-ana", roles = "MEMBER")
  @DisplayName("DELETE /api/gastos/{id} responde 204 y envía el id y el solicitante")
  void shouldDeleteGasto() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/api/gastos/{id}", GASTO_ID)).andExpect(status().isNoContent());
    verify(eliminarGastoUseCase)
        .execute(
            new EliminarGastoCommand(
                new Solicitante(new UserId("u-ana"), UserRole.MEMBER), GASTO_ID));
  }
}
