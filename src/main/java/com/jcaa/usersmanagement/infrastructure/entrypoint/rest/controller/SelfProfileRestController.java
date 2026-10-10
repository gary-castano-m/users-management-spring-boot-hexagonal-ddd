package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.DeleteUserUseCase;
import com.jcaa.usersmanagement.application.port.in.GetUserByIdUseCase;
import com.jcaa.usersmanagement.application.port.in.UpdateUserUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateUserCommand;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.UpdateProfileRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// ─────────────────────────────────────────────────────────────────────────────
// Perfil del usuario autenticado (patrón "/me").
//
// El id NUNCA viene de la URL ni del cuerpo: se toma del token JWT
// (authentication.getName() devuelve el "sub" del token). Así es imposible
// consultar o modificar el perfil de otra persona cambiando un id (IDOR).
// Reutiliza los mismos casos de uso que la gestión de usuarios del ADMIN.
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class SelfProfileRestController {

  private final GetUserByIdUseCase getUserByIdUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;

  @GetMapping
  @Operation(summary = "Consultar el perfil del usuario autenticado")
  public UserRestResponse getProfile(final Authentication authentication) {
    return UserRestMapper.toResponse(findCurrentUser(authentication));
  }

  @PutMapping
  @Operation(summary = "Actualizar el perfil del usuario autenticado (sin cambiar rol ni estado)")
  public UserRestResponse updateProfile(
      final Authentication authentication,
      @Valid @RequestBody final UpdateProfileRestRequest request) {
    final UserModel current = findCurrentUser(authentication);
    final UpdateUserCommand command = UserRestMapper.toSelfUpdateCommand(current, request);
    return UserRestMapper.toResponse(updateUserUseCase.execute(command));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Eliminar la cuenta del usuario autenticado")
  public void deleteProfile(final Authentication authentication) {
    deleteUserUseCase.execute(UserRestMapper.toDeleteCommand(authentication.getName()));
  }

  // El id del usuario actual sale del token, no del cliente
  private UserModel findCurrentUser(final Authentication authentication) {
    return getUserByIdUseCase.execute(UserRestMapper.toGetByIdQuery(authentication.getName()));
  }
}