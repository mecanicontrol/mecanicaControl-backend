package cl.mecanicontrol.backend.dto.inventario;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoRequestDTO(

        UUID proveedorId,

        @NotNull(message = "La categoría es obligatoria")
        UUID categoriaProductoId,

        UUID marcaProductoId,

        @NotNull(message = "El SKU es obligatorio")
        String codigoSku,

        @NotNull(message = "El nombre es obligatorio")
        String nombre,

        BigDecimal precioCosto,

        BigDecimal precioVenta,

        int stockMinimo,

        String ubicacionBodega
) {
}
