package com.jcaa.usersmanagement.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.jcaa.usersmanagement.application.port.out.SearchUsersPort;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.exception.InvalidUserRoleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para SearchUsersService.
 *
 * <p>Verifica la normalización de los criterios (rol sin distinguir mayúsculas, filtros vacíos
 * como "sin filtro", nombre sin espacios) y el rechazo de roles inexistentes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchUsersService")
class SearchUsersServiceTest {

  @Mock private SearchUsersPort searchUsersPort;

  @InjectMocks private SearchUsersService service;

  @Test
  @DisplayName("execute() convierte el rol al enum del dominio sin distinguir mayusculas")
  void shouldConvertRoleIgnoringCase() {
    // Act
    service.execute(new SearchUsersQuery("member", null));

    // Assert
    verify(searchUsersPort).search(UserRole.MEMBER, null);
  }

  @Test
  @DisplayName("execute() trata los filtros vacios como sin filtro y limpia el nombre")
  void shouldIgnoreBlankRoleAndTrimName() {
    // Act
    service.execute(new SearchUsersQuery("   ", "  ana  "));

    // Assert
    verify(searchUsersPort).search(null, "ana");
  }

  @Test
  @DisplayName("execute() rechaza un rol inexistente sin consultar la base de datos")
  void shouldRejectUnknownRole() {
    // Act & Assert
    assertThrows(
        InvalidUserRoleException.class,
        () -> service.execute(new SearchUsersQuery("SUPERUSER", null)));
    verifyNoInteractions(searchUsersPort);
  }
}