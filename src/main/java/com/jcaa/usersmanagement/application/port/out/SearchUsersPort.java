package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// Puerto de salida para los reportes de usuarios: búsqueda con filtros
// opcionales. Un filtro en null significa "no filtrar por ese criterio".
// Usa tipos del dominio (UserRole), no textos: la aplicación ya validó el rol.
// ─────────────────────────────────────────────────────────────────────────────
public interface SearchUsersPort {

  List<UserModel> search(UserRole role, String nameFragment);
}