package cl.mecanicontrol.backend.dto.ot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OTResponseDTO(
        UUID id,
        String codigoOt,
        String vehiculo,
        String cliente,
        String tecnico,
        String estado,
        BigDecimal costoManoObra,
        BigDecimal costoRepuestos,
        BigDecimal total,
        LocalDateTime fechaInicio,
        LocalDateTime fechaCierre
) { }
