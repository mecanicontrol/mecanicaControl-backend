package cl.mecanicontrol.backend.dto.ot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OTDetalleDTO(
        UUID id,
        String codigoOt,
        String estadoOt,
        String nombreCliente,
        String vehiculoPatente,
        String vehiculoDescripcion,
        String nombreTecnico,
        String nombreServicio,
        String diagnostico,
        BigDecimal costoManoObra,
        BigDecimal costoRepuestos,
        BigDecimal total,
        LocalDateTime fechaInicio,
        LocalDateTime fechaCierre,
        List<FaseDTO> fases
) { }