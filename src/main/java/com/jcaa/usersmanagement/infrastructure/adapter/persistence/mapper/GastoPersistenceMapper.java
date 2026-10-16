package com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper;

import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.entity.GastoEntity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// Traduce filas de la base de datos al modelo de dominio.
// Usa GastoModel.reconstruir(): los valores guardados no se recalculan, pero
// el dominio verifica que el total cuadre.
// ─────────────────────────────────────────────────────────────────────────────
public final class GastoPersistenceMapper {

  private GastoPersistenceMapper() {}

  public static GastoEntity fromResultSetToEntity(final ResultSet resultSet) throws SQLException {
    return new GastoEntity(
        resultSet.getString("id"),
        resultSet.getString("user_id"),
        resultSet.getString("user_name"),
        resultSet.getObject("fecha", LocalDate.class),
        resultSet.getBigDecimal("valor_total_sin_iva"),
        resultSet.getBigDecimal("iva_total"),
        resultSet.getBigDecimal("valor_total_con_iva"),
        resultSet.getString("lugar"),
        resultSet.getString("descripcion"));
  }

  public static GastoDetalle fromEntityToDetalle(final GastoEntity entity) {
    final GastoModel gasto =
        GastoModel.reconstruir(
            new GastoId(entity.id()),
            new UserId(entity.userId()),
            new GastoFecha(entity.fecha()),
            new Dinero(entity.valorTotalSinIva()),
            new Dinero(entity.ivaTotal()),
            new Dinero(entity.valorTotalConIva()),
            new GastoLugar(entity.lugar()),
            new GastoDescripcion(entity.descripcion()));
    return new GastoDetalle(gasto, entity.userName());
  }

  public static GastoDetalle fromResultSetToDetalle(final ResultSet resultSet)
      throws SQLException {
    return fromEntityToDetalle(fromResultSetToEntity(resultSet));
  }

  public static List<GastoDetalle> fromResultSetToDetalleList(final ResultSet resultSet)
      throws SQLException {
    final List<GastoDetalle> gastos = new ArrayList<>();
    while (resultSet.next()) {
      gastos.add(fromResultSetToDetalle(resultSet));
    }
    return gastos;
  }
}
