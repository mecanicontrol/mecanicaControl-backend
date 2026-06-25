package cl.mecanicontrol.backend.dto.reporte;

import java.math.BigDecimal;

public record ServicioEstadisticaDTO(
        String nombreServicio,
        int cantidadSolicitudes,
        BigDecimal ingresoGenerado
) {}