package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.GastoModel;

// Puerto de salida: actualizar un gasto existente (fecha, valores, lugar y descripción).
public interface UpdateGastoPort {
  void update(GastoModel gasto);
}
