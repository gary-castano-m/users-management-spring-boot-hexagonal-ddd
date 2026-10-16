package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.BuscarGastosUseCase;
import com.jcaa.usersmanagement.application.port.out.SearchGastosPort;
import com.jcaa.usersmanagement.application.service.dto.Solicitante;
import com.jcaa.usersmanagement.application.service.dto.query.BuscarGastosQuery;
import com.jcaa.usersmanagement.application.service.dto.result.ReporteGastos;
import com.jcaa.usersmanagement.domain.exception.InvalidGastoException;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.stereotype.Service;

// ─────────────────────────────────────────────────────────────────────────────
// Listado y reportes de gastos.
// - MEMBER: siempre se filtra por su propio id (solo ve lo suyo).
// - ADMIN y REVIEWER: sin filtro de dueño (ven los de todos).
// - Un rango con "desde" posterior a "hasta" se rechaza (400).
// - Un lugar vacío se trata como "sin filtro".
// Devuelve los gastos y sus totales (ReporteGastos).
// ─────────────────────────────────────────────────────────────────────────────
@Service
public class BuscarGastosService implements BuscarGastosUseCase {

  private final SearchGastosPort searchGastosPort;
  private final Validator validator;

  public BuscarGastosService(final SearchGastosPort searchGastosPort, final Validator validator) {
    this.searchGastosPort = searchGastosPort;
    this.validator = validator;
  }

  @Override
  public ReporteGastos execute(final BuscarGastosQuery query) {
    validate(query);
    if (query.desde() != null && query.hasta() != null && query.desde().isAfter(query.hasta())) {
      throw InvalidGastoException.becauseDateRangeIsInvalid();
    }

    final Solicitante solicitante = query.solicitante();
    final UserId propietario = solicitante.puedeVerTodos() ? null : solicitante.id();
    final String lugar =
        (query.lugar() == null || query.lugar().isBlank()) ? null : query.lugar().trim();

    return ReporteGastos.de(
        searchGastosPort.search(propietario, query.desde(), query.hasta(), lugar));
  }

  private <T> void validate(final T input) {
    final Set<ConstraintViolation<T>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
