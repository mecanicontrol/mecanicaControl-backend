package cl.mecanicontrol.backend.domain.diagnostico.dto;

import java.time.LocalDateTime;
import java.util.UUID;


public record DiagnosticoDTO(
    UUID id,
    UUID clienteId,
    String descripcionFallo,
    String marca,
    String modelo,
    Short anio,
    Integer kilometraje,
    String respuestaIa,
    String serviciosJson,
    String modeloIa,
    LocalDateTime createdAt
) {}