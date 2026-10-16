package com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

// Fila de la consulta de gastos tal como llega de la base de datos (gastos JOIN users).
public record GastoEntity(
    String id,
    String userId,
    String userName,
    LocalDate fecha,
    BigDecimal valorTotalSinIva,
    BigDecimal ivaTotal,
    BigDecimal valorTotalConIva,
    String lugar,
    String descripcion) {}
