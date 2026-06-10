package cl.mecanicontrol.backend.dto.pago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PagoRequestDTO(
        @NotNull(message = "La OT es obligatoria")
        UUID ordenTrabajoId,

        UUID clienteId,

        @NotNull(message = "El método de pago es obligatorio")
        String metodoPagoNombre,

        @Positive
        BigDecimal montoTotal,

        BigDecimal montoPagado,

        String referenciaExterna)
{ }
