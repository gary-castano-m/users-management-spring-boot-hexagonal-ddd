package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Tests para GastoRestMapper: el solicitante se arma solo con los datos del token. */
@DisplayName("GastoRestMapper")
class GastoRestMapperTest {

  @Test
  @DisplayName("toSolicitante() toma el id y el rol de la autenticación")
  void shouldBuildSolicitanteFromAuthentication() {
    // Arrange
    final UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "u-ana", null, List.of(new SimpleGrantedAuthority("ROLE_REVIEWER")));

    // Act
    final Solicitante solicitante = GastoRestMapper.toSolicitante(authentication);

    // Assert
    assertEquals(new Solicitante(new UserId("u-ana"), UserRole.REVIEWER), solicitante);
  }

  @Test
  @DisplayName("toSolicitante() falla si la autenticación no trae rol")
  void shouldFailWithoutRole() {
    // Arrange
    final UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken("u-ana", null, List.of());

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> GastoRestMapper.toSolicitante(authentication));
  }
}
