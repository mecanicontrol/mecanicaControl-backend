package cl.mecanicontrol.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record DiagnosticoRequestDTO(
    @NotBlank(message = "La descripción del fallo es obligatoria")
    String descripcionFallo,
    String marca,
    String modelo,
    Short anio,
    Integer kilometraje,
    String patente
) {}
