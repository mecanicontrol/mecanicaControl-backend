package cl.mecanicontrol.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CotizacionRequestDTO(
    @NotNull(message = "El servicio es obligatorio")
    UUID servicioId,
    @NotNull(message = "La marca del vehículo es obligatoria")
    UUID marcaVehiculoId,
    @NotNull(message = "El modelo del vehículo es obligatorio")
    UUID modeloVehiculoId,
    @NotNull(message = "El año es obligatorio")
    Short anio,
    String patente,
    UUID clienteId
) {}
