package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.command.ActualizarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.command.RegistrarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.application.service.dto.result.ReporteGastos;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.GastoRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.GastoRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ReporteGastosRestResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

// ─────────────────────────────────────────────────────────────────────────────
// Traduce entre la API REST y la capa de aplicación.
// toSolicitante(): el id sale del "sub" del token (authentication.getName())
// y el rol de su autoridad ROLE_xxx. El cliente nunca envía estos datos.
// ─────────────────────────────────────────────────────────────────────────────
public final class GastoRestMapper {

  private static final String ROLE_PREFIX = "ROLE_";

  private GastoRestMapper() {}

  public static Solicitante toSolicitante(final Authentication authentication) {
    final UserRole rol =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith(ROLE_PREFIX))
            .map(authority -> UserRole.fromString(authority.substring(ROLE_PREFIX.length())))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Authenticated user without role"));
    return new Solicitante(new UserId(authentication.getName()), rol);
  }

  public static RegistrarGastoCommand toRegistrarCommand(
      final Solicitante solicitante, final GastoRestRequest request) {
    return new RegistrarGastoCommand(
        solicitante,
        request.fecha(),
        request.valorTotalSinIva(),
        request.lugar(),
        request.descripcion());
  }

  public static ActualizarGastoCommand toActualizarCommand(
      final Solicitante solicitante, final String gastoId, final GastoRestRequest request) {
    return new ActualizarGastoCommand(
        solicitante,
        gastoId,
        request.fecha(),
        request.valorTotalSinIva(),
        request.lugar(),
        request.descripcion());
  }

  public static GastoRestResponse toResponse(final GastoDetalle detalle) {
    final GastoModel gasto = detalle.gasto();
    return new GastoRestResponse(
        gasto.getId().value(),
        gasto.getFecha().value(),
        gasto.getValorTotalSinIva().value(),
        gasto.getIvaTotal().value(),
        gasto.getValorTotalConIva().value(),
        gasto.getUserId().value(),
        detalle.nombreUsuario(),
        gasto.getLugar().value(),
        gasto.getDescripcion().value());
  }

  public static ReporteGastosRestResponse toReporteResponse(final ReporteGastos reporte) {
    return new ReporteGastosRestResponse(
        reporte.cantidad(),
        reporte.totalSinIva().value(),
        reporte.totalIva().value(),
        reporte.totalConIva().value(),
        reporte.gastos().stream().map(GastoRestMapper::toResponse).toList());
  }
}
