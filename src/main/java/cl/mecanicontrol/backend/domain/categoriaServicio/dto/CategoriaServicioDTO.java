package cl.mecanicontrol.backend.domain.categoriaServicio.dto;

import java.util.UUID;

public record  CategoriaServicioDTO(
    UUID id,
    String nombre,
    String descripcion

) {}

