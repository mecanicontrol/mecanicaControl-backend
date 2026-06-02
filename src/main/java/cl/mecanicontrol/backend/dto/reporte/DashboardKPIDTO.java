package cl.mecanicontrol.backend.dto.reporte;

import java.math.BigDecimal;

public record DashboardKPIDTO(
        int otActivas,
        int otCompletadasMes,
        BigDecimal ingresosMes,
        BigDecimal ingresosTotal,
        int tecnicosDisponibles,
        int clientesTotales,
        int clientesNuevosMes,
        String servicioTopNombre,
        int servicioTopCount
) {}