package com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository;

import com.jcaa.usersmanagement.application.port.out.DeleteGastoPort;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveGastoPort;
import com.jcaa.usersmanagement.application.port.out.SearchGastosPort;
import com.jcaa.usersmanagement.application.port.out.UpdateGastoPort;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.mapper.GastoPersistenceMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

// ─────────────────────────────────────────────────────────────────────────────
// Adaptador de persistencia de gastos (JDBC puro, como el de usuarios).
// Implementa los 5 puertos de salida de gastos. Las consultas hacen JOIN con
// users para devolver el nombre del dueño (GastoDetalle).
// ─────────────────────────────────────────────────────────────────────────────
@Repository
public class GastoRepositoryPostgres
    implements SaveGastoPort, UpdateGastoPort, DeleteGastoPort, GetGastoByIdPort, SearchGastosPort {

  private static final String SQL_INSERT =
      "INSERT INTO gastos "
          + "(id, user_id, fecha, valor_total_sin_iva, iva_total, valor_total_con_iva, "
          + "lugar, descripcion, created_at, updated_at) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

  private static final String SQL_UPDATE =
      "UPDATE gastos SET fecha = ?, valor_total_sin_iva = ?, iva_total = ?, "
          + "valor_total_con_iva = ?, lugar = ?, descripcion = ?, updated_at = NOW() "
          + "WHERE id = ?";

  private static final String SQL_DELETE = "DELETE FROM gastos WHERE id = ?";

  private static final String SQL_SELECT_BASE =
      "SELECT g.id, g.user_id, u.name AS user_name, g.fecha, g.valor_total_sin_iva, "
          + "g.iva_total, g.valor_total_con_iva, g.lugar, g.descripcion "
          + "FROM gastos g JOIN users u ON u.id = g.user_id";

  private static final String SQL_SELECT_BY_ID = SQL_SELECT_BASE + " WHERE g.id = ?";

  private static final String SQL_ORDER_BY_FECHA = " ORDER BY g.fecha DESC, g.created_at DESC";

  private final DataSource dataSource;

  public GastoRepositoryPostgres(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void save(final GastoModel gasto) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
      statement.setString(1, gasto.getId().value());
      statement.setString(2, gasto.getUserId().value());
      statement.setObject(3, gasto.getFecha().value());
      statement.setBigDecimal(4, gasto.getValorTotalSinIva().value());
      statement.setBigDecimal(5, gasto.getIvaTotal().value());
      statement.setBigDecimal(6, gasto.getValorTotalConIva().value());
      statement.setString(7, gasto.getLugar().value());
      statement.setString(8, gasto.getDescripcion().value());
      statement.executeUpdate();
    } catch (final SQLException exception) {
      throw PersistenceException.becauseGastoOperationFailed(
          "save", gasto.getId().value(), exception);
    }
  }

  @Override
  public void update(final GastoModel gasto) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
      statement.setObject(1, gasto.getFecha().value());
      statement.setBigDecimal(2, gasto.getValorTotalSinIva().value());
      statement.setBigDecimal(3, gasto.getIvaTotal().value());
      statement.setBigDecimal(4, gasto.getValorTotalConIva().value());
      statement.setString(5, gasto.getLugar().value());
      statement.setString(6, gasto.getDescripcion().value());
      statement.setString(7, gasto.getId().value());
      statement.executeUpdate();
    } catch (final SQLException exception) {
      throw PersistenceException.becauseGastoOperationFailed(
          "update", gasto.getId().value(), exception);
    }
  }

  @Override
  public void delete(final GastoId gastoId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {
      statement.setString(1, gastoId.value());
      statement.executeUpdate();
    } catch (final SQLException exception) {
      throw PersistenceException.becauseGastoOperationFailed("delete", gastoId.value(), exception);
    }
  }

  @Override
  public Optional<GastoDetalle> getById(final GastoId gastoId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {
      statement.setString(1, gastoId.value());
      final ResultSet resultSet = statement.executeQuery();
      if (!resultSet.next()) {
        return Optional.empty();
      }
      return Optional.of(GastoPersistenceMapper.fromResultSetToDetalle(resultSet));
    } catch (final SQLException exception) {
      throw PersistenceException.becauseGastoOperationFailed("find", gastoId.value(), exception);
    }
  }

  // ───────────────────────────────────────────────────────────────────────────
  // Listado y reportes. Cada filtro es opcional y el WHERE se arma solo con los
  // que llegan. Los VALORES nunca se concatenan en el SQL: viajan como
  // parámetros (?), lo que previene la inyección SQL.
  // ───────────────────────────────────────────────────────────────────────────
  @Override
  public List<GastoDetalle> search(
      final UserId propietario,
      final LocalDate desde,
      final LocalDate hasta,
      final String lugar) {
    final List<String> conditions = new ArrayList<>();
    final List<Object> parameters = new ArrayList<>();

    if (propietario != null) {
      conditions.add("g.user_id = ?");
      parameters.add(propietario.value());
    }
    if (desde != null) {
      conditions.add("g.fecha >= ?");
      parameters.add(desde);
    }
    if (hasta != null) {
      conditions.add("g.fecha <= ?");
      parameters.add(hasta);
    }
    if (lugar != null && !lugar.isBlank()) {
      // ILIKE: coincidencia parcial sin distinguir mayúsculas (propio de PostgreSQL)
      conditions.add("g.lugar ILIKE ?");
      parameters.add("%" + lugar.trim() + "%");
    }

    final String sql =
        SQL_SELECT_BASE
            + (conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions))
            + SQL_ORDER_BY_FECHA;

    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(sql)) {
      for (int index = 0; index < parameters.size(); index++) {
        statement.setObject(index + 1, parameters.get(index));
      }
      final ResultSet resultSet = statement.executeQuery();
      return GastoPersistenceMapper.fromResultSetToDetalleList(resultSet);
    } catch (final SQLException exception) {
      throw PersistenceException.becauseGastoOperationFailed("search", "filtros", exception);
    }
  }
}
