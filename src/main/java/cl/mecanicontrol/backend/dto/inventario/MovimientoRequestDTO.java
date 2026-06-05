package cl.mecanicontrol.backend.dto.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record MovimientoRequestDTO(

        @NotNull(message = "El id del producto es obligatorio")
        UUID productoId,

        UUID ordenTrabajoId,

        @NotBlank
        String tipoMovimientoNombre,

        @Positive
        int cantidad,

        BigDecimal precioUnitario,

        String motivo
) {
}
