package cl.mecanicontrol.backend.dto;

import java.util.UUID;

public record VehiculoResponseDTO(
        UUID id,
        String patente,
        UUID marcaVehiculoId,
        String marcaNombre,
        UUID modeloVehiculoId,
        String modeloNombre,
        Short anio,
        Integer kilometrajeIngreso,
        String alias
) {}
