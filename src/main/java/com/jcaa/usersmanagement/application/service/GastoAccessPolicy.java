package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.exception.GastoAccessDeniedException;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;

// ─────────────────────────────────────────────────────────────────────────────
// Reglas de acceso a un gasto, compartidas por consultar, actualizar y eliminar.
//
// 1. Si el solicitante no puede VER el gasto (no es suyo y no es ADMIN ni
//    REVIEWER) → 404, igual que si no existiera. Un 403 confirmaría que ese
//    id existe (protección contra enumeración).
// 2. Si puede verlo pero no MODIFICARLO (REVIEWER frente a un gasto ajeno)
//    → 403.
// ─────────────────────────────────────────────────────────────────────────────
final class GastoAccessPolicy {

  private GastoAccessPolicy() {}

  static GastoDetalle buscarVisibleOFallar(
      final GetGastoByIdPort getGastoByIdPort, final GastoId gastoId, final Solicitante solicitante) {
    return getGastoByIdPort
        .getById(gastoId)
        .filter(detalle -> puedeVer(detalle, solicitante))
        .orElseThrow(() -> GastoNotFoundException.becauseIdWasNotFound(gastoId.value()));
  }

  static void exigirPermisoDeModificacion(
      final GastoDetalle detalle, final Solicitante solicitante) {
    final boolean esDueno = detalle.gasto().perteneceA(solicitante.id());
    if (!esDueno && !solicitante.puedeModificarTodos()) {
      throw GastoAccessDeniedException.becauseCannotModify(detalle.gasto().getId().value());
    }
  }

  private static boolean puedeVer(final GastoDetalle detalle, final Solicitante solicitante) {
    return solicitante.puedeVerTodos() || detalle.gasto().perteneceA(solicitante.id());
  }
}
