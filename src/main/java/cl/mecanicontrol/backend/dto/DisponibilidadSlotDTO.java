package cl.mecanicontrol.backend.dto;

import java.util.UUID;

public record DisponibilidadSlotDTO(
    String fechaHoraInicio,
    String fechaHoraFin,
    boolean disponible,
    UUID tecnicoId,
    String nombreTecnico
) {}
