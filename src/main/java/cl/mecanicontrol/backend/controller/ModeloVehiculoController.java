package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.service.ModeloVehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/modelos")
@RequiredArgsConstructor
public class ModeloVehiculoController {

    private final ModeloVehiculoService modeloVehiculoService;

    @GetMapping("/listar")
    public ResponseEntity<List<ModeloVehiculo>> findAll() {
        return ResponseEntity.ok(modeloVehiculoService.findAll());
    }

    @GetMapping("/marca/{id}")
    public ResponseEntity<List<ModeloVehiculo>> findByMarca(@PathVariable("id") UUID idMarca) {
        return ResponseEntity.ok(modeloVehiculoService.findByMarcaId(idMarca));
    }

    @PostMapping("/save/modelo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModeloVehiculo> saveModelo(@RequestBody ModeloVehiculo modeloVehiculo) {
        return ResponseEntity.ok(modeloVehiculoService.saveModelo(modeloVehiculo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModeloVehiculo> updateModelo(@PathVariable UUID id,
                                                        @RequestBody Map<String, String> body) {
        UUID marcaId = body.get("marcaId") != null ? UUID.fromString(body.get("marcaId")) : null;
        return ResponseEntity.ok(modeloVehiculoService.updateModelo(id, body.get("nombre"), marcaId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteModelo(@PathVariable UUID id) {
        modeloVehiculoService.deleteModelo(id);
        return ResponseEntity.noContent().build();
    }
}