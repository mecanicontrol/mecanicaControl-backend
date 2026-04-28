package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CotizacionResponseDTO(
    UUID id,
    String nombreServicio,
    String descripcionServicio,
    String nombreMarca,
    String nombreModelo,
    Short anio,
    String patente,
    BigDecimal precioEstimado,
    LocalDateTime expiraAt,
    LocalDateTime createdAt
) {}
