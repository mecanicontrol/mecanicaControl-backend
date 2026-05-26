package cl.mecanicontrol.backend.dto.ot;

import java.math.BigDecimal;
import java.util.UUID;

public record MaterialDTO(
        UUID productoId,
        String nombreProducto,
        String sku,
        int cantidad,
        BigDecimal precioUnitario
) { }
