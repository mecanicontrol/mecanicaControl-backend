package cl.mecanicontrol.backend.dto.admin;

import java.util.List;
import java.util.Map;

public record DashboardDTO(
        long agendamientosHoy,
        long agendamientosPendientes,
        long tecnicosDisponibles,
        long totalClientes,
        Map<String, Long> agendamientosPorEstado,
        List<ServicioConteoDTO> serviciosMasSolicitados,
        List<AgendamientoResumenDTO> ultimosAgendamientos,
        List<AgendamientoResumenDTO> proximosHoy
) {
    public record ServicioConteoDTO(String nombre, long conteo) {}

    public record AgendamientoResumenDTO(
            String id,
            String clienteNombre,
            String servicioNombre,
            String vehiculoPatente,
            String estado,
            String fechaInicio
    ) {}
}
