package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

// Un gasto tal como lo devuelve la API (campos del ejercicio 6 más el id del dueño).
public record GastoRestResponse(
    String id,
    LocalDate fecha,
    BigDecimal valorTotalSinIva,
    BigDecimal ivaTotal,
    BigDecimal valorTotalConIva,
    String usuarioId,
    String nombreUsuario,
    String lugar,
    String descripcion) {}
