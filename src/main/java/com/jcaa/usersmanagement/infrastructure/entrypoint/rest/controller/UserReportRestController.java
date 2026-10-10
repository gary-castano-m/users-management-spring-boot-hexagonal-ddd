package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.SearchUsersUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ─────────────────────────────────────────────────────────────────────────────
// Reportes parametrizados de usuarios (solo ADMIN y REVIEWER, por la regla
// de seguridad existente sobre GET /api/users/**).
//   GET /api/users/reports?role=MEMBER   → reporte por rol
//   GET /api/users/reports?name=ana      → reporte por nombre (parcial)
// Los parámetros son opcionales y se pueden combinar.
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/users/reports")
@RequiredArgsConstructor
public class UserReportRestController {

  private final SearchUsersUseCase searchUsersUseCase;

  @GetMapping
  @Operation(summary = "Reporte de usuarios filtrado por rol y/o nombre")
  public List<UserRestResponse> report(
      @RequestParam(required = false) final String role,
      @RequestParam(required = false) final String name) {
    return searchUsersUseCase.execute(new SearchUsersQuery(role, name)).stream()
        .map(UserRestMapper::toResponse)
        .toList();
  }
}