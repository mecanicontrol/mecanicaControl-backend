package cl.mecanicontrol.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SeguimientoResponseDTO(
    UUID id,
    String codigoOt,
    String estadoOt,
    String vehiculoPatente,
    String vehiculoDescripcion,
    String nombreCliente,
    String nombreTecnico,
    String nombreServicio,
    LocalDateTime fechaInicio,
    LocalDateTime fechaCierre,
    List<FaseSeguimientoDTO> fases,
    PrediccionSeguimientoDTO prediccion,
    BigDecimal costoManoObra,
    BigDecimal costoRepuestos,
    BigDecimal total,
    String diagnostico,
    String trabajoRealizado
) {}