package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrdenTrabajoListDTO(
    UUID id,
    String codigoOt,
    String estadoOt,
    String nombreCliente,
    String vehiculoPatente,
    String nombreTecnico,
    String nombreServicio,
    LocalDateTime fechaInicio,
    LocalDateTime fechaCierre,
    BigDecimal total
) {}