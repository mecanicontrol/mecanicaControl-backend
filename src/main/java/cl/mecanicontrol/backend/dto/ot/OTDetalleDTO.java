package cl.mecanicontrol.backend.dto.ot;

import java.util.List;

public record OTDetalleDTO(
        OTResponseDTO ot,
        List<FaseDTO> fases,
        List<MaterialDTO> materiales
) { }
