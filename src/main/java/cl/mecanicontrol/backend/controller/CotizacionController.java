package cl.mecanicontrol.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.dto.CotizacionRequestDTO;
import cl.mecanicontrol.backend.dto.CotizacionResponseDTO;
import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import cl.mecanicontrol.backend.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final MarcaVehiculoRepository marcaRepo;
    private final ModeloVehiculoRepository modeloRepo;

    @PostMapping("/api/cotizaciones")
    public ResponseEntity<CotizacionResponseDTO> crear(@Valid @RequestBody CotizacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.crear(request));
    }

    @GetMapping("/api/cotizaciones/cliente/{clienteId}")
    public ResponseEntity<List<CotizacionResponseDTO>> porCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(cotizacionService.findByCliente(clienteId));
    }

    // Endpoints públicos para poblar los dropdowns del frontend
    @GetMapping("/api/marcas")
    public ResponseEntity<List<MarcaVehiculo>> listarMarcas() {
        return ResponseEntity.ok(marcaRepo.findAll());
    }

    @GetMapping("/api/marcas/{marcaId}/modelos")
    public ResponseEntity<List<ModeloVehiculo>> listarModelos(@PathVariable UUID marcaId) {
        return ResponseEntity.ok(modeloRepo.findByMarcaId(marcaId));
    }
}
