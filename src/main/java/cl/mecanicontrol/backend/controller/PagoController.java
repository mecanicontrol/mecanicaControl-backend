package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.pago.PagoRequestDTO;
import cl.mecanicontrol.backend.dto.pago.PagoResponseDTO;
import cl.mecanicontrol.backend.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<PagoResponseDTO> registrar(@Valid @RequestBody PagoRequestDTO dto){
        return ResponseEntity.status(201).body(pagoService.registrar(dto));
    }

    @GetMapping("/ot/{otId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<List<PagoResponseDTO>> findByOT(@PathVariable UUID otId){
        return ResponseEntity.ok(pagoService.findByOT(otId));
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<List<PagoResponseDTO>> findByCliente(@PathVariable UUID clienteId){
        return ResponseEntity.ok(pagoService.findByCliente(clienteId));
    }
}
