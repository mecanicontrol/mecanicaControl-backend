package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record PrediccionSeguimientoDTO(
    int tiempoEstimadoMin,
    LocalDateTime horaFinEstimada,
    BigDecimal confianzaPct,
    Map<String, Integer> desglosePorFase
) {}