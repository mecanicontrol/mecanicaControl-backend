package cl.mecanicontrol.backend.dto.ot;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OTRequestDTO(

        @NotNull(message = "El agendamiento es obligatorio")
        UUID agendamientoId,

        UUID tecnicoId
) { }
