package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import java.util.Optional;

// Puerto de salida: consultar un gasto (con el nombre de su dueño) por su id.
public interface GetGastoByIdPort {
  Optional<GastoDetalle> getById(GastoId gastoId);
}
