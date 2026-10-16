package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.GastoModel;

// Puerto de salida: guardar un gasto nuevo.
public interface SaveGastoPort {
  void save(GastoModel gasto);
}
