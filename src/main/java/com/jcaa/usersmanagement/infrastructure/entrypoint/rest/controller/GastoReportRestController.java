package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.BuscarGastosUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.BuscarGastosQuery;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ReporteGastosRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.GastoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ─────────────────────────────────────────────────────────────────────────────
// Reportes parametrizados de gastos (con totales):
//   Reporte 1: GET /api/gastos/reports?desde=2026-10-01&hasta=2026-10-31
//   Reporte 2: GET /api/gastos/reports?lugar=exito
// Los filtros se pueden combinar. Un MEMBER solo obtiene sus propios gastos.
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/gastos/reports")
public class GastoReportRestController {

  private final BuscarGastosUseCase buscarGastosUseCase;

  public GastoReportRestController(final BuscarGastosUseCase buscarGastosUseCase) {
    this.buscarGastosUseCase = buscarGastosUseCase;
  }

  @GetMapping
  @Operation(summary = "Reporte de gastos por rango de fechas (desde/hasta) y/o por lugar")
  public ReporteGastosRestResponse reporte(
      final Authentication authentication,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate hasta,
      @RequestParam(required = false) final String lugar) {
    final BuscarGastosQuery query =
        new BuscarGastosQuery(GastoRestMapper.toSolicitante(authentication), desde, hasta, lugar);
    return GastoRestMapper.toReporteResponse(buscarGastosUseCase.execute(query));
  }
}
