package cl.mecanicontrol.backend.dto.ot;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SeguimientoPublicoDTO(
        String codigoOt,
        String vehiculo,
        String estado,
        List<FaseDTO> fases,
        FaseDTO faseActual,
        PrediccionDTO prediccion,
        String nombreServicio,
        String nombreTecnico,
        LocalDateTime fechaInicio,
        BigDecimal total,
        BigDecimal costoManoObra,
        BigDecimal costoRepuestos
) { }