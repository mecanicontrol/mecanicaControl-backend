package cl.mecanicontrol.backend.dto.ot;

import java.time.LocalDateTime;
import java.util.UUID;

public record FaseDTO(
        UUID id,
        String fase,
        int orden,
        String checklistJson,
        String observaciones,
        String imagenes,
        LocalDateTime inicioAt,
        LocalDateTime finAt,
        Long duracionMinutos,
        String aprobacionEstado,
        String aprobacionNota
) { }
