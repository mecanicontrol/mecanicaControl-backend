package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;

public record ServicioRecomendadoDTO(
    String nombre,
    String descripcion,
    String probabilidad,
    BigDecimal precioBase,
    String urgencia

) {

}
