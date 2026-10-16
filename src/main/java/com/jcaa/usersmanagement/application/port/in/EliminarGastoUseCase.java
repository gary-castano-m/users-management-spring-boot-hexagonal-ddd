package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.EliminarGastoCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Puerto de entrada: eliminar un gasto (propio, o cualquiera si es ADMIN).
public interface EliminarGastoUseCase {
  void execute(@NotNull @Valid EliminarGastoCommand command);
}
