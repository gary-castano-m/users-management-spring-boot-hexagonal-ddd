package com.jcaa.usersmanagement.domain.model;

import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.math.BigDecimal;
import java.util.Objects;

// ─────────────────────────────────────────────────────────────────────────────
// Un gasto registrado por un usuario de Oye!.
//
// Regla de negocio central: el IVA (19 %) lo calcula el dominio. El cliente
// solo envía el valor sin IVA; el IVA y el total se derivan aquí, así ningún
// cliente (Kodular, Android, Swagger) puede guardar totales que no cuadren.
//
// Es inmutable: actualizar() devuelve un gasto nuevo con el IVA recalculado.
// El dueño (userId) y la identidad (id) nunca cambian.
// ─────────────────────────────────────────────────────────────────────────────
public final class GastoModel {

  public static final BigDecimal TASA_IVA = new BigDecimal("0.19");

  private final GastoId id;
  private final UserId userId;
  private final GastoFecha fecha;
  private final Dinero valorTotalSinIva;
  private final Dinero ivaTotal;
  private final Dinero valorTotalConIva;
  private final GastoLugar lugar;
  private final GastoDescripcion descripcion;

  private GastoModel(
      final GastoId id,
      final UserId userId,
      final GastoFecha fecha,
      final Dinero valorTotalSinIva,
      final Dinero ivaTotal,
      final Dinero valorTotalConIva,
      final GastoLugar lugar,
      final GastoDescripcion descripcion) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.userId = Objects.requireNonNull(userId, "userId cannot be null");
    this.fecha = Objects.requireNonNull(fecha, "fecha cannot be null");
    this.valorTotalSinIva = Objects.requireNonNull(valorTotalSinIva, "valorTotalSinIva cannot be null");
    this.ivaTotal = Objects.requireNonNull(ivaTotal, "ivaTotal cannot be null");
    this.valorTotalConIva = Objects.requireNonNull(valorTotalConIva, "valorTotalConIva cannot be null");
    this.lugar = Objects.requireNonNull(lugar, "lugar cannot be null");
    this.descripcion = Objects.requireNonNull(descripcion, "descripcion cannot be null");
  }

  /** Registra un gasto nuevo: calcula el IVA y el total a partir del valor sin IVA. */
  public static GastoModel registrar(
      final GastoId id,
      final UserId userId,
      final GastoFecha fecha,
      final Dinero valorTotalSinIva,
      final GastoLugar lugar,
      final GastoDescripcion descripcion) {
    final Dinero iva = calcularIva(valorTotalSinIva);
    return new GastoModel(
        id, userId, fecha, valorTotalSinIva, iva, valorTotalSinIva.sumar(iva), lugar, descripcion);
  }

  /**
   * Reconstruye un gasto ya guardado (por ejemplo, leído de la base de datos). No recalcula: usa
   * los valores almacenados, pero verifica que el total cuadre.
   */
  public static GastoModel reconstruir(
      final GastoId id,
      final UserId userId,
      final GastoFecha fecha,
      final Dinero valorTotalSinIva,
      final Dinero ivaTotal,
      final Dinero valorTotalConIva,
      final GastoLugar lugar,
      final GastoDescripcion descripcion) {
    if (!valorTotalSinIva.sumar(ivaTotal).equals(valorTotalConIva)) {
      throw InvalidGastoException.becauseTotalsAreInconsistent();
    }
    return new GastoModel(
        id, userId, fecha, valorTotalSinIva, ivaTotal, valorTotalConIva, lugar, descripcion);
  }

  /** Devuelve una copia con los nuevos datos y el IVA recalculado. Conserva id y dueño. */
  public GastoModel actualizar(
      final GastoFecha nuevaFecha,
      final Dinero nuevoValorSinIva,
      final GastoLugar nuevoLugar,
      final GastoDescripcion nuevaDescripcion) {
    return registrar(id, userId, nuevaFecha, nuevoValorSinIva, nuevoLugar, nuevaDescripcion);
  }

  /** Indica si el gasto pertenece al usuario dado. */
  public boolean perteneceA(final UserId otroUserId) {
    return userId.equals(otroUserId);
  }

  private static Dinero calcularIva(final Dinero valorSinIva) {
    return valorSinIva.porcentaje(TASA_IVA);
  }

  public GastoId getId() {
    return id;
  }

  public UserId getUserId() {
    return userId;
  }

  public GastoFecha getFecha() {
    return fecha;
  }

  public Dinero getValorTotalSinIva() {
    return valorTotalSinIva;
  }

  public Dinero getIvaTotal() {
    return ivaTotal;
  }

  public Dinero getValorTotalConIva() {
    return valorTotalConIva;
  }

  public GastoLugar getLugar() {
    return lugar;
  }

  public GastoDescripcion getDescripcion() {
    return descripcion;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) return true;
    if (!(other instanceof GastoModel gasto)) return false;
    return id.equals(gasto.id)
        && userId.equals(gasto.userId)
        && fecha.equals(gasto.fecha)
        && valorTotalSinIva.equals(gasto.valorTotalSinIva)
        && ivaTotal.equals(gasto.ivaTotal)
        && valorTotalConIva.equals(gasto.valorTotalConIva)
        && lugar.equals(gasto.lugar)
        && descripcion.equals(gasto.descripcion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, userId, fecha, valorTotalSinIva, ivaTotal, valorTotalConIva, lugar, descripcion);
  }

  @Override
  public String toString() {
    return "GastoModel[id=" + id + ", userId=" + userId + ", fecha=" + fecha
        + ", valorTotalSinIva=" + valorTotalSinIva + ", ivaTotal=" + ivaTotal
        + ", valorTotalConIva=" + valorTotalConIva + ", lugar=" + lugar + "]";
  }
}
