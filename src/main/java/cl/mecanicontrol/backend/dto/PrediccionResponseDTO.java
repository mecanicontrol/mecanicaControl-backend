package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record PrediccionResponseDTO(
    String codigoOt,
    int tiempoEstimadoMin,
    LocalDateTime horaInicioEstimada,
    LocalDateTime horaFinEstimada,
    BigDecimal confianzaPct,
    Map<String, Integer> desglosePorFase,
    String modeloIa
) {}