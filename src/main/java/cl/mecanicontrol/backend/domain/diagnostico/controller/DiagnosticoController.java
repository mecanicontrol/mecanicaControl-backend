package cl.mecanicontrol.backend.domain.diagnostico.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.domain.diagnostico.dto.DiagnosticoRequestDTO;
import cl.mecanicontrol.backend.domain.diagnostico.dto.DiagnosticoResponseDTO;
import cl.mecanicontrol.backend.domain.diagnostico.service.DiagnosticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/ia")
@RequiredArgsConstructor
public class DiagnosticoController {
    private final DiagnosticoService diagnosticoService;

    @PostMapping("/diagnosticar")
    public ResponseEntity<DiagnosticoResponseDTO> diagnosticar(
        @Valid @RequestBody DiagnosticoRequestDTO request) {
        DiagnosticoResponseDTO response = 
            diagnosticoService.diagnosticar(request);
        return ResponseEntity.ok(response);
    }

}
