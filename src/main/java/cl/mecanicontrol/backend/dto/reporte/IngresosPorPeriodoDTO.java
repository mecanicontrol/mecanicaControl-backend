package cl.mecanicontrol.backend.dto.reporte;

import java.math.BigDecimal;

public record IngresosPorPeriodoDTO(
        String periodo,
        BigDecimal totalIngresos,
        int cantidadOT
) {}