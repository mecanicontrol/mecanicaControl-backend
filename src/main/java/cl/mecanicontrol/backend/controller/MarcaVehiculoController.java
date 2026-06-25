package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.service.MarcaVehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/marcas")
@RequiredArgsConstructor
public class MarcaVehiculoController {

    private final MarcaVehiculoService marcaVehiculoService;

    @GetMapping("/listar")
    public ResponseEntity<List<MarcaVehiculo>> findAll() {
        return ResponseEntity.ok(marcaVehiculoService.findAll());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MarcaVehiculo> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(marcaVehiculoService.findByIdMarca(id));
    }

    @PostMapping("/save/marca")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcaVehiculo> createMarca(@RequestBody MarcaVehiculo marcaVehiculo) {
        return ResponseEntity.ok(marcaVehiculoService.createMarca(marcaVehiculo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcaVehiculo> updateMarca(@PathVariable UUID id,
                                                      @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(marcaVehiculoService.updateMarca(id, body.get("nombre")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMarca(@PathVariable UUID id) {
        marcaVehiculoService.deleteMarca(id);
        return ResponseEntity.noContent().build();
    }
}