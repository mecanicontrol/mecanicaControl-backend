package cl.mecanicontrol.backend.dto.inventario;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoResponseDTO(

        UUID id,

        String sku,

        String nombre,

        String categoria,

        String marca,

        BigDecimal precioCosto,

        BigDecimal precioVenta,

        int stockActual,

        int stockMinimo,

        Boolean alertaStock

) {
}
