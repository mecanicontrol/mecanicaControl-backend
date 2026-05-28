package cl.mecanicontrol.backend.dto;

import java.time.LocalDateTime;

public record FaseSeguimientoDTO(
    String nombre,
    int orden,
    String estado,           // COMPLETADA | ACTIVA | PENDIENTE
    LocalDateTime inicioAt,
    LocalDateTime finAt,
    Integer tiempoEstimadoMin
) {}