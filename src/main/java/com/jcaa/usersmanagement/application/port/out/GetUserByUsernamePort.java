package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserUsername;
import java.util.Optional;

// Puerto de salida: buscar un usuario por su username (login y unicidad).
public interface GetUserByUsernamePort {
  Optional<UserModel> getByUsername(UserUsername username);
}
