package com.jcaa.usersmanagement.application.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests para Solicitante: las reglas de permisos por rol sobre los gastos. */
@DisplayName("Solicitante")
class SolicitanteTest {

  private static final UserId ID = new UserId("u-001");

  @Test
  @DisplayName("ADMIN y REVIEWER ven los gastos de todos; MEMBER no")
  void shouldAllowAdminAndReviewerToSeeAll() {
    // Act & Assert
    assertAll(
        () -> assertTrue(new Solicitante(ID, UserRole.ADMIN).puedeVerTodos()),
        () -> assertTrue(new Solicitante(ID, UserRole.REVIEWER).puedeVerTodos()),
        () -> assertFalse(new Solicitante(ID, UserRole.MEMBER).puedeVerTodos()));
  }

  @Test
  @DisplayName("solo ADMIN modifica gastos ajenos")
  void shouldAllowOnlyAdminToModifyAll() {
    // Act & Assert
    assertAll(
        () -> assertTrue(new Solicitante(ID, UserRole.ADMIN).puedeModificarTodos()),
        () -> assertFalse(new Solicitante(ID, UserRole.REVIEWER).puedeModificarTodos()),
        () -> assertFalse(new Solicitante(ID, UserRole.MEMBER).puedeModificarTodos()));
  }
}
