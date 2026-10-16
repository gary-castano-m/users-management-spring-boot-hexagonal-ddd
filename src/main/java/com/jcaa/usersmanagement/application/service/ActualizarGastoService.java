package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.ActualizarGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.UpdateGastoPort;
import com.jcaa.usersmanagement.application.service.dto.command.ActualizarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.model.GastoModel;
import com.jcaa.usersmanagement.domain.valueobject.Dinero;
import com.jcaa.usersmanagement.domain.valueobject.GastoDescripcion;
import com.jcaa.usersmanagement.domain.valueobject.GastoFecha;
import com.jcaa.usersmanagement.domain.valueobject.GastoId;
import com.jcaa.usersmanagement.domain.valueobject.GastoLugar;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import org.springframework.stereotype.Service;

// ─────────────────────────────────────────────────────────────────────────────
// Actualiza un gasto: primero verifica que el solicitante pueda verlo (404 si
// no) y modificarlo (403 si no). El dominio recalcula el IVA y conserva el
// dueño original, aunque quien actualice sea un ADMIN.
// ─────────────────────────────────────────────────────────────────────────────
@Service
public class ActualizarGastoService implements ActualizarGastoUseCase {

  private final GetGastoByIdPort getGastoByIdPort;
  private final UpdateGastoPort updateGastoPort;
  private final Validator validator;

  public ActualizarGastoService(
      final GetGastoByIdPort getGastoByIdPort,
      final UpdateGastoPort updateGastoPort,
      final Validator validator) {
    this.getGastoByIdPort = getGastoByIdPort;
    this.updateGastoPort = updateGastoPort;
    this.validator = validator;
  }

  @Override
  public GastoDetalle execute(final ActualizarGastoCommand command) {
    validate(command);

    final GastoDetalle actual =
        GastoAccessPolicy.buscarVisibleOFallar(
            getGastoByIdPort, new GastoId(command.gastoId()), command.solicitante());
    GastoAccessPolicy.exigirPermisoDeModificacion(actual, command.solicitante());

    final GastoModel actualizado =
        actual
            .gasto()
            .actualizar(
                new GastoFecha(command.fecha()),
                new Dinero(command.valorTotalSinIva()),
                new GastoLugar(command.lugar()),
                new GastoDescripcion(command.descripcion()));
    updateGastoPort.update(actualizado);

    return new GastoDetalle(actualizado, actual.nombreUsuario());
  }

  private <T> void validate(final T input) {
    final Set<ConstraintViolation<T>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
