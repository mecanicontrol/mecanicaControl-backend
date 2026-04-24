package cl.mecanicontrol.backend.dto;

import java.util.UUID;

public record  CategoriaServicioDTO(
    UUID id,
    String nombre,
    String descripcion

) {}

