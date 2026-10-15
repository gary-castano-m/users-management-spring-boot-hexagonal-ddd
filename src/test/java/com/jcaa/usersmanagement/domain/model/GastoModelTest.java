package com.jcaa.usersmanagement.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests para GastoModel.
 *
 * <p>Verifica la regla central del dominio: el IVA (19 %) y el total se calculan a partir del
 * valor sin IVA, al registrar y al actualizar, y un gasto reconstruido debe tener totales
 * consistentes.
 */
@DisplayName("GastoModel")
class GastoModelTest {

  private GastoId id;
  private UserId owner;
  private GastoFecha fecha;
  private GastoLugar lugar;
  private GastoDescripcion descripcion;

  @BeforeEach
  void setUp() {
    id = new GastoId("g-001");
    owner = new UserId("u-001");
    fecha = new GastoFecha(LocalDate.of(2026, 10, 1));
    lugar = new GastoLugar("Exito San Diego");
    descripcion = new GastoDescripcion("Mercado de la semana");
  }

  @Test
  @DisplayName("registrar() calcula el IVA del 19 % y el total")
  void shouldCalculateVatAndTotalOnRegister() {
    // Act
    final GastoModel gasto =
        GastoModel.registrar(id, owner, fecha, Dinero.of("100000"), lugar, descripcion);

    // Assert
    assertAll(
        "valores calculados por el dominio",
        () -> assertEquals(Dinero.of("100000.00"), gasto.getValorTotalSinIva()),
        () -> assertEquals(Dinero.of("19000.00"), gasto.getIvaTotal()),
        () -> assertEquals(Dinero.of("119000.00"), gasto.getValorTotalConIva()),
        () -> assertSame(owner, gasto.getUserId()));
  }

  @Test
  @DisplayName("registrar() conserva los centavos del IVA")
  void shouldKeepCentsOnRegister() {
    // Act
    final GastoModel gasto =
        GastoModel.registrar(id, owner, fecha, Dinero.of("10050"), lugar, descripcion);

    // Assert
    assertAll(
        () -> assertEquals(Dinero.of("1909.50"), gasto.getIvaTotal()),
        () -> assertEquals(Dinero.of("11959.50"), gasto.getValorTotalConIva()));
  }

  @Test
  @DisplayName("actualizar() recalcula el IVA y conserva el id y el dueño")
  void shouldRecalculateVatOnUpdate() {
    // Arrange
    final GastoModel original =
        GastoModel.registrar(id, owner, fecha, Dinero.of("100000"), lugar, descripcion);

    // Act
    final GastoModel updated =
        original.actualizar(
            fecha, Dinero.of("200"), new GastoLugar("Olimpica"), descripcion);

    // Assert
    assertAll(
        () -> assertNotSame(original, updated, "debe ser una nueva instancia"),
        () -> assertEquals(id, updated.getId(), "el id no cambia"),
        () -> assertEquals(owner, updated.getUserId(), "el dueño no cambia"),
        () -> assertEquals(Dinero.of("38.00"), updated.getIvaTotal()),
        () -> assertEquals(Dinero.of("238.00"), updated.getValorTotalConIva()),
        () -> assertEquals("Olimpica", updated.getLugar().value()));
  }

  @Test
  @DisplayName("perteneceA() distingue al dueño de otros usuarios")
  void shouldKnowItsOwner() {
    // Arrange
    final GastoModel gasto =
        GastoModel.registrar(id, owner, fecha, Dinero.of("100"), lugar, descripcion);

    // Act & Assert
    assertAll(
        () -> assertTrue(gasto.perteneceA(new UserId("u-001"))),
        () -> assertFalse(gasto.perteneceA(new UserId("u-999"))));
  }

  @Test
  @DisplayName("reconstruir() conserva los valores guardados cuando cuadran")
  void shouldRebuildWithStoredValues() {
    // Act
    final GastoModel gasto =
        GastoModel.reconstruir(
            id,
            owner,
            fecha,
            Dinero.of("100"),
            Dinero.of("19"),
            Dinero.of("119"),
            lugar,
            descripcion);

    // Assert
    assertEquals(
        GastoModel.registrar(id, owner, fecha, Dinero.of("100"), lugar, descripcion), gasto);
  }

  @Test
  @DisplayName("reconstruir() rechaza totales que no cuadran")
  void shouldRejectInconsistentTotals() {
    // Act & Assert
    assertThrows(
        InvalidGastoException.class,
        () ->
            GastoModel.reconstruir(
                id,
                owner,
                fecha,
                Dinero.of("100"),
                Dinero.of("19"),
                Dinero.of("120"),
                lugar,
                descripcion));
  }
}
