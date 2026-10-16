package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.ActualizarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Puerto de entrada: actualizar un gasto (propio, o cualquiera si es ADMIN).
public interface ActualizarGastoUseCase {
  GastoDetalle execute(@NotNull @Valid ActualizarGastoCommand command);
}
