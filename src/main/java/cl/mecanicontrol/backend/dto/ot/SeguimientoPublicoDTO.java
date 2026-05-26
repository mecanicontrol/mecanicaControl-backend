package cl.mecanicontrol.backend.dto.ot;

import java.util.List;

public record SeguimientoPublicoDTO(
        String codigoOt,
        String vehiculo,
        String estado,
        List<FaseDTO> fases,
        FaseDTO faseActual,
        PrediccionDTO prediccion
) { }
