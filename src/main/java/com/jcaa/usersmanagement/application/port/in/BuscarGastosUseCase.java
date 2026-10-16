package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.BuscarGastosQuery;
import com.jcaa.usersmanagement.application.service.dto.result.ReporteGastos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Puerto de entrada: listar gastos y generar los reportes por fechas y por lugar.
public interface BuscarGastosUseCase {
  ReporteGastos execute(@NotNull @Valid BuscarGastosQuery query);
}
