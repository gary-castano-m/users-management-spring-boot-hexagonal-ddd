package com.jcaa.usersmanagement.domain.valueobject;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

// ─────────────────────────────────────────────────────────────────────────────
// Fecha en que se realizó un gasto. Es obligatoria y no puede estar en el
// futuro: un gasto registra algo que ya pasó.
//
// "Hoy" se calcula con la zona horaria de Colombia y no con la del servidor:
// Render trabaja en UTC, y a las 8 p. m. en Colombia en UTC ya es el día
// siguiente.
// ─────────────────────────────────────────────────────────────────────────────
public record GastoFecha(LocalDate value) {

  public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

  public GastoFecha {
    if (Objects.isNull(value)) {
      throw InvalidGastoException.becauseDateIsNull();
    }
    if (value.isAfter(LocalDate.now(ZONA_COLOMBIA))) {
      throw InvalidGastoException.becauseDateIsInTheFuture();
    }
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
