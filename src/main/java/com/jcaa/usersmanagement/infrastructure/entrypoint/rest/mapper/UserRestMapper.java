package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateUserCommand;
import com.jcaa.usersmanagement.application.service.dto.command.DeleteUserCommand;
import com.jcaa.usersmanagement.application.service.dto.command.UpdateUserCommand;
import com.jcaa.usersmanagement.application.service.dto.query.GetUserByIdQuery;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateUserRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.RegisterRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.UpdateUserRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.UserRestResponse;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class UserRestMapper {

     // El registro público siempre crea usuarios MEMBER: quien se registra
     // no puede elegir su rol (evita que alguien se registre como ADMIN).
  private static final String SELF_REGISTRATION_ROLE = UserRole.MEMBER.name();

  public CreateUserCommand toCreateCommand(final CreateUserRestRequest request) {
    return new CreateUserCommand(
        // El id lo genera el servidor, no el cliente
        UserId.generate().value(),
        request.name(),
        request.email(),
        request.password(),
        request.role());
  }

  public CreateUserCommand toRegisterCommand(final RegisterRestRequest request) {
       return new CreateUserCommand(
           UserId.generate().value(),
           request.name(),
           request.email(),
           request.password(),
           SELF_REGISTRATION_ROLE);
     }

  public UpdateUserCommand toUpdateCommand(final String id, final UpdateUserRestRequest request) {
    return new UpdateUserCommand(
        id,
        request.name(),
        request.email(),
        request.password(),
        request.role(),
        request.status());
  }

  public GetUserByIdQuery toGetByIdQuery(final String id) {
    return new GetUserByIdQuery(id);
  }

  public DeleteUserCommand toDeleteCommand(final String id) {
    return new DeleteUserCommand(id);
  }

  public UserRestResponse toResponse(final UserModel user) {
    return new UserRestResponse(
        user.getId().value(),
        user.getName().value(),
        user.getEmail().value(),
        user.getRole().name(),
        user.getStatus().name());
  }

  public List<UserRestResponse> toResponseList(final List<UserModel> users) {
    return users.stream().map(UserRestMapper::toResponse).toList();
  }
}

