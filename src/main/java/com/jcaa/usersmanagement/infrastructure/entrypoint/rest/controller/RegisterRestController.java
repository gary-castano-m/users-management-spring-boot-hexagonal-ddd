package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.CreateUserUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.RegisterRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.UserRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// ─────────────────────────────────────────────────────────────────────────────
// Registro público de usuarios. Es un endpoint separado de POST /api/users
// (reservado al ADMIN) para que nadie pueda elegir su propio rol al
// registrarse. Reutiliza el mismo caso de uso que la creación por el admin.
// ─────────────────────────────────────────────────────────────────────────────
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterRestController {

  private final CreateUserUseCase createUserUseCase;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Registrar un nuevo usuario (rol MEMBER, activo de inmediato)")
  public UserRestResponse register(@Valid @RequestBody final RegisterRestRequest request) {
    final CreateUserCommand command = UserRestMapper.toRegisterCommand(request);
    final UserModel user = createUserUseCase.execute(command);
    return UserRestMapper.toResponse(user);
  }
}