package cl.mecanicontrol.backend.domain.diagnostico.service;

import cl.mecanicontrol.backend.domain.diagnostico.dto.DiagnosticoRequestDTO;
import cl.mecanicontrol.backend.domain.diagnostico.dto.DiagnosticoResponseDTO;

public interface DiagnosticoService {
    DiagnosticoResponseDTO diagnosticar(DiagnosticoRequestDTO request);

}
