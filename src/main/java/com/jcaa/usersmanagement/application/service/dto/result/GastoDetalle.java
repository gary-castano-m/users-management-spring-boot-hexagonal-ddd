package com.jcaa.usersmanagement.application.service.dto.result;

import com.jcaa.usersmanagement.domain.model.GastoModel;
import java.util.Objects;

// ─────────────────────────────────────────────────────────────────────────────
// Modelo de lectura: un gasto junto con el nombre de su dueño.
//
// El nombre NO se guarda en la tabla gastos (quedaría desactualizado si el
// usuario cambia su nombre): se obtiene con un JOIN a users al consultar.
// Así la API puede devolver "nombreUsuario", como pide el enunciado.
// ─────────────────────────────────────────────────────────────────────────────
public record GastoDetalle(GastoModel gasto, String nombreUsuario) {

  public GastoDetalle {
    Objects.requireNonNull(gasto, "gasto cannot be null");
    Objects.requireNonNull(nombreUsuario, "nombreUsuario cannot be null");
  }
}
