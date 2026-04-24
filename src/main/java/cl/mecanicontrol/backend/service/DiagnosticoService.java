package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.DiagnosticoRequestDTO;
import cl.mecanicontrol.backend.dto.DiagnosticoResponseDTO;

public interface DiagnosticoService {
    DiagnosticoResponseDTO diagnosticar(DiagnosticoRequestDTO request);

}
