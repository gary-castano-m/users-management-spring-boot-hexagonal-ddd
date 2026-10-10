package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.SearchUsersUseCase;
import com.jcaa.usersmanagement.application.port.out.SearchUsersPort;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// ─────────────────────────────────────────────────────────────────────────────
// Caso de uso de los reportes de usuarios.
// Normaliza los criterios antes de consultar:
//   - un filtro vacío se trata como "sin filtro" (null);
//   - el rol se convierte al enum del dominio, sin distinguir mayúsculas.
//     Un rol inexistente lanza InvalidUserRoleException (la API responde 400).
// ─────────────────────────────────────────────────────────────────────────────
@Service
@RequiredArgsConstructor
public class SearchUsersService implements SearchUsersUseCase {

  private final SearchUsersPort searchUsersPort;

  @Override
  public List<UserModel> execute(final SearchUsersQuery query) {
    final UserRole role = isBlank(query.role()) ? null : UserRole.fromString(query.role());
    final String name = isBlank(query.name()) ? null : query.name().trim();
    return searchUsersPort.search(role, name);
  }

  private static boolean isBlank(final String value) {
    return value == null || value.isBlank();
  }
}