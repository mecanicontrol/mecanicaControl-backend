package cl.mecanicontrol.backend.service;

import java.util.List;
import java.util.UUID;

import cl.mecanicontrol.backend.dto.CotizacionRequestDTO;
import cl.mecanicontrol.backend.dto.CotizacionResponseDTO;

public interface CotizacionService {
    CotizacionResponseDTO crear(CotizacionRequestDTO request);
    List<CotizacionResponseDTO> findByCliente(UUID clienteId);
}
