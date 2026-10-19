package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.ActualizarGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.BuscarGastosUseCase;
import com.jcaa.usersmanagement.application.port.in.ConsultarGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.EliminarGastoUseCase;
import com.jcaa.usersmanagement.application.port.in.RegistrarGastoUseCase;
import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.command.EliminarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.query.BuscarGastosQuery;
import com.jcaa.usersmanagement.application.service.dto.query.ConsultarGastoQuery;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.GastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.GastoRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.GastoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// ─────────────────────────────────────────────────────────────────────────────
// CRUD de gastos. Requiere un usuario autenticado (cualquier rol).
// Quién pide y con qué rol sale del token (Solicitante); las reglas de qué
// puede ver o modificar cada rol están en la capa de aplicación.
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/gastos")
public class GastoRestController {

  private final RegistrarGastoUseCase registrarGastoUseCase;
  private final ActualizarGastoUseCase actualizarGastoUseCase;
  private final EliminarGastoUseCase eliminarGastoUseCase;
  private final ConsultarGastoUseCase consultarGastoUseCase;
  private final BuscarGastosUseCase buscarGastosUseCase;

  public GastoRestController(
      final RegistrarGastoUseCase registrarGastoUseCase,
      final ActualizarGastoUseCase actualizarGastoUseCase,
      final EliminarGastoUseCase eliminarGastoUseCase,
      final ConsultarGastoUseCase consultarGastoUseCase,
      final BuscarGastosUseCase buscarGastosUseCase) {
    this.registrarGastoUseCase = registrarGastoUseCase;
    this.actualizarGastoUseCase = actualizarGastoUseCase;
    this.eliminarGastoUseCase = eliminarGastoUseCase;
    this.consultarGastoUseCase = consultarGastoUseCase;
    this.buscarGastosUseCase = buscarGastosUseCase;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Registrar un gasto propio (el IVA del 19 % lo calcula el backend)")
  public GastoRestResponse registrar(
      final Authentication authentication, @Valid @RequestBody final GastoRestRequest request) {
    final Solicitante solicitante = GastoRestMapper.toSolicitante(authentication);
    return GastoRestMapper.toResponse(
        registrarGastoUseCase.execute(GastoRestMapper.toRegistrarCommand(solicitante, request)));
  }

  @GetMapping
  @Operation(summary = "Listar gastos (los propios; ADMIN y REVIEWER ven todos)")
  public List<GastoRestResponse> listar(final Authentication authentication) {
    final Solicitante solicitante = GastoRestMapper.toSolicitante(authentication);
    return buscarGastosUseCase
        .execute(new BuscarGastosQuery(solicitante, null, null, null))
        .gastos()
        .stream()
        .map(GastoRestMapper::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Consultar un gasto")
  public GastoRestResponse consultar(
      final Authentication authentication, @PathVariable final String id) {
    final Solicitante solicitante = GastoRestMapper.toSolicitante(authentication);
    return GastoRestMapper.toResponse(
        consultarGastoUseCase.execute(new ConsultarGastoQuery(solicitante, id)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Actualizar un gasto (propio; ADMIN cualquiera). Recalcula el IVA")
  public GastoRestResponse actualizar(
      final Authentication authentication,
      @PathVariable final String id,
      @Valid @RequestBody final GastoRestRequest request) {
    final Solicitante solicitante = GastoRestMapper.toSolicitante(authentication);
    return GastoRestMapper.toResponse(
        actualizarGastoUseCase.execute(
            GastoRestMapper.toActualizarCommand(solicitante, id, request)));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Eliminar un gasto (propio; ADMIN cualquiera)")
  public void eliminar(final Authentication authentication, @PathVariable final String id) {
    final Solicitante solicitante = GastoRestMapper.toSolicitante(authentication);
    eliminarGastoUseCase.execute(new EliminarGastoCommand(solicitante, id));
  }
}
