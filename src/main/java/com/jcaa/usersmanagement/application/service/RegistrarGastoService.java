package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.RegistrarGastoUseCase;
import com.jcaa.usersmanagement.application.port.out.GetGastoByIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveGastoPort;
import com.jcaa.usersmanagement.application.service.dto.command.RegistrarGastoCommand;
import com.jcaa.usersmanagement.application.service.dto.result.GastoDetalle;
import com.jcaa.usersmanagement.domain.exception.GastoNotFoundException;
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
// Registra un gasto. El dueño es SIEMPRE el solicitante (sale del token) y el
// IVA lo calcula GastoModel.registrar(). Después de guardarlo se vuelve a leer
// para devolverlo con el nombre del dueño.
// ─────────────────────────────────────────────────────────────────────────────
@Service
public class RegistrarGastoService implements RegistrarGastoUseCase {

  private final SaveGastoPort saveGastoPort;
  private final GetGastoByIdPort getGastoByIdPort;
  private final Validator validator;

  public RegistrarGastoService(
      final SaveGastoPort saveGastoPort,
      final GetGastoByIdPort getGastoByIdPort,
      final Validator validator) {
    this.saveGastoPort = saveGastoPort;
    this.getGastoByIdPort = getGastoByIdPort;
    this.validator = validator;
  }

  @Override
  public GastoDetalle execute(final RegistrarGastoCommand command) {
    validate(command);

    final GastoModel gasto =
        GastoModel.registrar(
            GastoId.generate(),
            command.solicitante().id(),
            new GastoFecha(command.fecha()),
            new Dinero(command.valorTotalSinIva()),
            new GastoLugar(command.lugar()),
            new GastoDescripcion(command.descripcion()));
    saveGastoPort.save(gasto);

    return getGastoByIdPort
        .getById(gasto.getId())
        .orElseThrow(() -> GastoNotFoundException.becauseIdWasNotFound(gasto.getId().value()));
  }

  private <T> void validate(final T input) {
    final Set<ConstraintViolation<T>> violations = validator.validate(input);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
