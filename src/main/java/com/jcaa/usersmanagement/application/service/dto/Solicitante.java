package com.jcaa.usersmanagement.application.service.dto;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.util.Objects;

// ─────────────────────────────────────────────────────────────────────────────
// Quién hace la petición: su id y su rol, tomados del token JWT.
// Concentra las reglas de permisos sobre gastos:
//   - ADMIN y REVIEWER pueden VER los gastos de todos.
//   - Solo ADMIN puede MODIFICAR o ELIMINAR gastos ajenos.
//   - Cualquiera puede ver, modificar y eliminar los suyos.
// ─────────────────────────────────────────────────────────────────────────────
public record Solicitante(UserId id, UserRole rol) {

  public Solicitante {
    Objects.requireNonNull(id, "id cannot be null");
    Objects.requireNonNull(rol, "rol cannot be null");
  }

  public boolean puedeVerTodos() {
    return rol == UserRole.ADMIN || rol == UserRole.REVIEWER;
  }

  public boolean puedeModificarTodos() {
    return rol == UserRole.ADMIN;
  }
}
