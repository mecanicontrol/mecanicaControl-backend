package cl.mecanicontrol.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DisponibilidadSlotDTO(
    LocalDateTime fechaHoraInicio,
    LocalDateTime fechaHoraFin,
    boolean disponible,
    UUID tecnicoId,
    String nombreTecnico
) {}
