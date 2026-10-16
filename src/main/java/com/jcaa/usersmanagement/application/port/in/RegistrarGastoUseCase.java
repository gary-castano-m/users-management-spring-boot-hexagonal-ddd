package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.RegistrarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Puerto de entrada: registrar un gasto del solicitante.
public interface RegistrarGastoUseCase {
  GastoDetalle execute(@NotNull @Valid RegistrarGastoCommand command);
}
