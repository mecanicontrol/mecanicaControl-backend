package cl.mecanicontrol.backend.dto;

public record VehiculoRequestDTO(
        String patente,
        String marcaId,
        String modeloId,
        Integer anio,
        Integer kilometraje,
        String alias
) {}
