package com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests para GastoRepositoryPostgres.
 *
 * <p>Usa mocks de JDBC para verificar qué SQL se prepara y qué parámetros se envían: guardar,
 * actualizar, eliminar, consultar por id y buscar con filtros opcionales (sin concatenar valores
 * en el SQL). Cada operación traduce un SQLException en PersistenceException.
 */
@DisplayName("GastoRepositoryPostgres")
@ExtendWith(MockitoExtension.class)
class GastoRepositoryPostgresTest {

  private static final String GASTO_ID = "g-001";
  private static final String USER_ID = "u-001";
  private static final String USER_NAME = "Ana Torres";
  private static final LocalDate FECHA = LocalDate.of(2026, 10, 1);
  private static final String LUGAR = "Exito San Diego";
  private static final String DESCRIPCION = "Mercado de la semana";

  @Mock private DataSource dataSource;
  @Mock private Connection connection;
  @Mock private PreparedStatement statement;
  @Mock private ResultSet resultSet;

  private GastoRepositoryPostgres repository;
  private GastoModel gasto;

  @BeforeEach
  void setUp() {
    repository = new GastoRepositoryPostgres(dataSource);
    gasto =
        GastoModel.registrar(
            new GastoId(GASTO_ID),
            new UserId(USER_ID),
            new GastoFecha(FECHA),
            Dinero.of("100"),
            new GastoLugar(LUGAR),
            new GastoDescripcion(DESCRIPCION));
  }

  // Helper: dataSource → connection → statement
  private void configureStatement() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(anyString())).thenReturn(statement);
  }

  // Helper: una fila completa de gastos JOIN users
  private void configureResultSetRow() throws SQLException {
    when(resultSet.getString("id")).thenReturn(GASTO_ID);
    when(resultSet.getString("user_id")).thenReturn(USER_ID);
    when(resultSet.getString("user_name")).thenReturn(USER_NAME);
    when(resultSet.getObject("fecha", LocalDate.class)).thenReturn(FECHA);
    when(resultSet.getBigDecimal("valor_total_sin_iva")).thenReturn(new BigDecimal("100.00"));
    when(resultSet.getBigDecimal("iva_total")).thenReturn(new BigDecimal("19.00"));
    when(resultSet.getBigDecimal("valor_total_con_iva")).thenReturn(new BigDecimal("119.00"));
    when(resultSet.getString("lugar")).thenReturn(LUGAR);
    when(resultSet.getString("descripcion")).thenReturn(DESCRIPCION);
  }

  // ── save()

  @Test
  @DisplayName("save() envía todos los valores del gasto como parámetros del INSERT")
  void shouldSaveGastoWithAllParameters() throws SQLException {
    // Arrange
    configureStatement();

    // Act
    repository.save(gasto);

    // Assert
    verify(statement).setString(1, GASTO_ID);
    verify(statement).setString(2, USER_ID);
    verify(statement).setObject(3, FECHA);
    verify(statement).setBigDecimal(4, new BigDecimal("100.00"));
    verify(statement).setBigDecimal(5, new BigDecimal("19.00"));
    verify(statement).setBigDecimal(6, new BigDecimal("119.00"));
    verify(statement).setString(7, LUGAR);
    verify(statement).setString(8, DESCRIPCION);
    verify(statement).executeUpdate();
  }

  @Test
  @DisplayName("save() lanza PersistenceException si el INSERT falla")
  void shouldThrowWhenSaveFails() throws SQLException {
    // Arrange
    configureStatement();
    when(statement.executeUpdate()).thenThrow(new SQLException("insert failed"));

    // Act & Assert
    assertThrows(PersistenceException.class, () -> repository.save(gasto));
  }

  // ── update()

  @Test
  @DisplayName("update() envía los nuevos valores y el id al final del UPDATE")
  void shouldUpdateGastoWithIdAsLastParameter() throws SQLException {
    // Arrange
    configureStatement();

    // Act
    repository.update(gasto);

    // Assert
    verify(statement).setObject(1, FECHA);
    verify(statement).setBigDecimal(2, new BigDecimal("100.00"));
    verify(statement).setBigDecimal(3, new BigDecimal("19.00"));
    verify(statement).setBigDecimal(4, new BigDecimal("119.00"));
    verify(statement).setString(5, LUGAR);
    verify(statement).setString(6, DESCRIPCION);
    verify(statement).setString(7, GASTO_ID);
    verify(statement).executeUpdate();
  }

  @Test
  @DisplayName("update() lanza PersistenceException si el UPDATE falla")
  void shouldThrowWhenUpdateFails() throws SQLException {
    // Arrange
    configureStatement();
    when(statement.executeUpdate()).thenThrow(new SQLException("update failed"));

    // Act & Assert
    assertThrows(PersistenceException.class, () -> repository.update(gasto));
  }

  // ── delete()

  @Test
  @DisplayName("delete() envía el id como parámetro del DELETE")
  void shouldDeleteGastoById() throws SQLException {
    // Arrange
    configureStatement();

    // Act
    repository.delete(new GastoId(GASTO_ID));

    // Assert
    verify(statement).setString(1, GASTO_ID);
    verify(statement).executeUpdate();
  }

  @Test
  @DisplayName("delete() lanza PersistenceException si no hay conexión")
  void shouldThrowWhenDeleteFails() throws SQLException {
    // Arrange
    when(dataSource.getConnection()).thenThrow(new SQLException("no connection"));

    // Act & Assert
    assertThrows(PersistenceException.class, () -> repository.delete(new GastoId(GASTO_ID)));
  }

  // ── getById()

  @Test
  @DisplayName("getById() devuelve el gasto con el nombre de su dueño")
  void shouldReturnGastoDetalleWhenFound() throws SQLException {
    // Arrange
    configureStatement();
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    configureResultSetRow();

    // Act
    final Optional<GastoDetalle> result = repository.getById(new GastoId(GASTO_ID));

    // Assert
    assertTrue(result.isPresent());
    final GastoDetalle detalle = result.get();
    assertAll(
        "gasto leído de la base de datos",
        () -> assertEquals(gasto, detalle.gasto()),
        () -> assertEquals(USER_NAME, detalle.nombreUsuario()));
    verify(statement).setString(1, GASTO_ID);
  }

  @Test
  @DisplayName("getById() devuelve vacío si el gasto no existe")
  void shouldReturnEmptyWhenNotFound() throws SQLException {
    // Arrange
    configureStatement();
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    // Act & Assert
    assertTrue(repository.getById(new GastoId(GASTO_ID)).isEmpty());
  }

  @Test
  @DisplayName("getById() lanza PersistenceException si la consulta falla")
  void shouldThrowWhenGetByIdFails() throws SQLException {
    // Arrange
    when(dataSource.getConnection()).thenThrow(new SQLException("no connection"));

    // Act & Assert
    assertThrows(PersistenceException.class, () -> repository.getById(new GastoId(GASTO_ID)));
  }

  // ── search()

  @Test
  @DisplayName("search() sin filtros no agrega WHERE ni parámetros")
  void shouldSearchWithoutFilters() throws SQLException {
    // Arrange
    configureStatement();
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);
    final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    // Act
    final List<GastoDetalle> result = repository.search(null, null, null, "  ");

    // Assert
    verify(connection).prepareStatement(sqlCaptor.capture());
    assertAll(
        () -> assertTrue(result.isEmpty()),
        () -> assertFalse(sqlCaptor.getValue().contains("WHERE"), "sin filtros no hay WHERE"),
        () -> assertTrue(sqlCaptor.getValue().endsWith("ORDER BY g.fecha DESC, g.created_at DESC")));
    verify(statement, never()).setObject(anyInt(), any());
  }

  @Test
  @DisplayName("search() con todos los filtros los envía como parámetros, nunca en el SQL")
  void shouldSearchWithAllFiltersAsParameters() throws SQLException {
    // Arrange
    configureStatement();
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true, false);
    configureResultSetRow();
    final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    final LocalDate desde = LocalDate.of(2026, 10, 1);
    final LocalDate hasta = LocalDate.of(2026, 10, 31);

    // Act
    final List<GastoDetalle> result =
        repository.search(new UserId(USER_ID), desde, hasta, " exito ");

    // Assert
    verify(connection).prepareStatement(sqlCaptor.capture());
    final String sql = sqlCaptor.getValue();
    assertAll(
        () -> assertEquals(1, result.size()),
        () ->
            assertTrue(
                sql.contains(
                    "WHERE g.user_id = ? AND g.fecha >= ? AND g.fecha <= ? AND g.lugar ILIKE ?")),
        () -> assertFalse(sql.contains("exito"), "el valor no se concatena en el SQL"));
    verify(statement).setObject(1, USER_ID);
    verify(statement).setObject(2, desde);
    verify(statement).setObject(3, hasta);
    verify(statement).setObject(4, "%exito%");
  }

  @Test
  @DisplayName("search() lanza PersistenceException si la consulta falla")
  void shouldThrowWhenSearchFails() throws SQLException {
    // Arrange
    when(dataSource.getConnection()).thenThrow(new SQLException("no connection"));

    // Act & Assert
    assertThrows(PersistenceException.class, () -> repository.search(null, null, null, null));
  }
}
