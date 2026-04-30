package cl.mecanicontrol.backend.dto.catalogo;

import java.util.UUID;

public record CatalogoDTO(
        UUID id,
        String nombre,
        String descripcion
)
{ }
