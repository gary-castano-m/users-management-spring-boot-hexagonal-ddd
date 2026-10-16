package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.valueobject.GastoId;

// Puerto de salida: eliminar un gasto.
public interface DeleteGastoPort {
  void delete(GastoId gastoId);
}
