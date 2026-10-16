package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.EliminarGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.DeleteGastoPort;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.service.dto.command.EliminarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.stereotype.Service;

// Elimina un gasto aplicando las mismas reglas de acceso que la actualización.
@Service
public class EliminarGastoService implements EliminarGastoUseCase {

  private final GetGastoByIdPort getGastoByIdPort;
  private final DeleteGastoPort deleteGastoPort;
  private final Validator validator;

  public EliminarGastoService(
      final GetGastoByIdPort getGastoByIdPort,
      final DeleteGastoPort deleteGastoPort,
      final Validator validator) {
    this.getGastoByIdPort = getGastoByIdPort;
    this.deleteGastoPort = deleteGastoPort;
    this.validator = validator;
  }

  @Override
  public void execute(final EliminarGastoCommand command) {
    validate(command);

    final GastoDetalle actual =
        GastoAccessPolicy.buscarVisibleOFallar(
            getGastoByIdPort, new GastoId(command.gastoId()), command.solicitante());
    GastoAccessPolicy.exigirPermisoDeModificacion(actual, command.solicitante());

    deleteGastoPort.delete(actual.gasto().getId());
  }

  private <T> void validate(final T input) {
    final Set<ConstraintViolation<T>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
