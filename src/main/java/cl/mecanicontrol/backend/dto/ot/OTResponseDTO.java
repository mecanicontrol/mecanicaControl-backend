package cl.mecanicontrol.backend.dto.ot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OTResponseDTO(
        UUID id,
        String codigoOt,
        String estadoOt,
        String nombreCliente,
        String vehiculoPatente,
        String nombreTecnico,
        String nombreServicio,
        BigDecimal costoManoObra,
        BigDecimal costoRepuestos,
        BigDecimal total,
        LocalDateTime fechaInicio,
        LocalDateTime fechaCierre
) { }