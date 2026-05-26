package cl.mecanicontrol.backend.dto.ot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrediccionDTO(
        int tiempoEstimadoMin,
        BigDecimal confianzaPct,
        LocalDateTime horaInicioEstimada,
        LocalDateTime horaFinEstimada,
        String modeloIa
) { }
