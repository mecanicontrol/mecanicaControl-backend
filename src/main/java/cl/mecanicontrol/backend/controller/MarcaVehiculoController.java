package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.service.MarcaVehiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/marcas")
@RequiredArgsConstructor
public class MarcaVehiculoController {
    private final MarcaVehiculoService marcaVehiculoService;


    @GetMapping("/listar")
    public ResponseEntity<List<MarcaVehiculo>> findAll(){
        return ResponseEntity.ok(marcaVehiculoService.findAll());
    }

    @GetMapping("id/{id}")
    public ResponseEntity<MarcaVehiculo> findByIdMarca(UUID idMarca){
        return ResponseEntity.ok(marcaVehiculoService.findByIdMarca(idMarca));
    }

    @PostMapping("/save/marca")
    public ResponseEntity<MarcaVehiculo> createMarca(MarcaVehiculo marcaVehiculo){
        return ResponseEntity.ok(marcaVehiculoService.createMarca(marcaVehiculo));
    }
}
