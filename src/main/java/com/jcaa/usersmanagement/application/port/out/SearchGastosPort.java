package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import java.time.LocalDate;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// Puerto de salida: listar gastos con filtros opcionales (null = sin filtro).
//   propietario → solo los gastos de ese usuario (null = de todos)
//   desde/hasta → rango de fechas, ambos inclusive (reporte 1)
//   lugar       → coincidencia parcial sin distinguir mayúsculas (reporte 2)
// Sin filtros, devuelve el listado completo.
// ─────────────────────────────────────────────────────────────────────────────
public interface SearchGastosPort {
  List<GastoDetalle> search(UserId propietario, LocalDate desde, LocalDate hasta, String lugar);
}
