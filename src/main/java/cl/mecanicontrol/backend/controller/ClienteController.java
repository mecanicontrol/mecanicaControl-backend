package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.cliente.ClienteResponseDTO;
import cl.mecanicontrol.backend.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> findAll(){
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findById(@PathVariable UUID id){
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PatchMapping("/{id}/puntos")
    public ResponseEntity<Void> addPuntos(
            @PathVariable UUID id,
            @RequestParam int puntos){
        clienteService.addPuntos(id, puntos);
        return ResponseEntity.ok().build();
    }
}
