package cl.mecanicontrol.backend.controller;


import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.service.ModeloVehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/modelos")
@RequiredArgsConstructor
public class ModeloVehiculoController {
    private final ModeloVehiculoService modeloVehiculoService;

    @GetMapping("/listar")
    public ResponseEntity<List<ModeloVehiculo>> findAll(){
        return ResponseEntity.ok(modeloVehiculoService.findAll());
    }

    @GetMapping("marca/{id}")
    public ResponseEntity<List<ModeloVehiculo>> findByMarcaVehiculoId(@PathVariable("id") UUID idMarca){
        return ResponseEntity.ok(modeloVehiculoService.findByMarcaId(idMarca));
    }

    @PostMapping("save/modelo")
    public ResponseEntity<ModeloVehiculo> saveModelo(ModeloVehiculo modeloVehiculo){
        return ResponseEntity.ok(modeloVehiculoService.saveModelo(modeloVehiculo));
    }
}
