package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.ConsultarGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.service.dto.query.ConsultarGastoQuery;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.stereotype.Service;

// Consulta un gasto: lo devuelve solo si el solicitante puede verlo (si no, 404).
@Service
public class ConsultarGastoService implements ConsultarGastoUseCase {

  private final GetGastoByIdPort getGastoByIdPort;
  private final Validator validator;

  public ConsultarGastoService(final GetGastoByIdPort getGastoByIdPort, final Validator validator) {
    this.getGastoByIdPort = getGastoByIdPort;
    this.validator = validator;
  }

  @Override
  public GastoDetalle execute(final ConsultarGastoQuery query) {
    validate(query);
    return GastoAccessPolicy.buscarVisibleOFallar(
        getGastoByIdPort, new GastoId(query.gastoId()), query.solicitante());
  }

  private <T> void validate(final T input) {
    final Set<ConstraintViolation<T>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
