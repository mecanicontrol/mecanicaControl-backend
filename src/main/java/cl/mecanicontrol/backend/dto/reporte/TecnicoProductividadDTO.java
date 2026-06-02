package cl.mecanicontrol.backend.dto.reporte;

import java.math.BigDecimal;

public record TecnicoProductividadDTO(
        String nombreTecnico,
        int otCompletadas,
        int otActivas,
        BigDecimal ingresoGenerado
) {}