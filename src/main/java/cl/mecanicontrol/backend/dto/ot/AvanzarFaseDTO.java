package cl.mecanicontrol.backend.dto.ot;

import java.util.UUID;

public record AvanzarFaseDTO(
        UUID faseId,
        String checklistJson,
        String observaciones
) { }
