package cl.mecanicontrol.backend.dto;

import java.util.List;

public record DiagnosticoResponseDTO(
    String resumenDiagnostico,
    String nivelUrgencia,
    String recomendacionGeneral,
    List<ServicioRecomendadoDTO> serviciosRecomendados,
    String patente,
    String marca,
    String modelo,
    Short anio,
    Integer kilometraje
) {}
