package cl.mecanicontrol.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.dto.SeguimientoResponseDTO;
import cl.mecanicontrol.backend.service.SeguimientoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seguimiento")
@RequiredArgsConstructor
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    // GET /api/seguimiento/{codigo} — público, clientes consultan su OT
    @GetMapping("/{codigo}")
    public ResponseEntity<SeguimientoResponseDTO> buscar(@PathVariable String codigo) {
        return ResponseEntity.ok(seguimientoService.buscarPorCodigo(codigo));
    }
}