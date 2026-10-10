package com.jcaa.usersmanagement.application.service.dto.query;

// ─────────────────────────────────────────────────────────────────────────────
// Criterios de los reportes de usuarios. Ambos son opcionales:
//   role → reporte por rol    (ej.: "MEMBER")
//   name → reporte por nombre (coincidencia parcial)
// Llegan como texto desde la entrada; el servicio los valida y normaliza.
// ─────────────────────────────────────────────────────────────────────────────
public record SearchUsersQuery(String role, String name) {}