package cl.mecanicontrol.backend.dto.catalogo;

import java.util.UUID;

public record ModeloVehiculoDTO(
        UUID id,
        UUID marcaId,
        String nombreMarca,
        String nombre
) {
}
