package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicioCatalogoDTO(
    UUID id,
    String nombre,
    String descripcion,
    Integer duracionMinutos,
    BigDecimal precioBase,
    String categoriaNombre

){}
