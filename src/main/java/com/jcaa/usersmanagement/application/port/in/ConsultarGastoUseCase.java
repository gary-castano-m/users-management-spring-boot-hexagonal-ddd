package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.ConsultarGastoQuery;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Puerto de entrada: consultar un gasto visible para el solicitante.
public interface ConsultarGastoUseCase {
  GastoDetalle execute(@NotNull @Valid ConsultarGastoQuery query);
}
