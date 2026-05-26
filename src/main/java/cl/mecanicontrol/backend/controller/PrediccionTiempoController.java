package cl.mecanicontrol.backend.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.dto.PrediccionResponseDTO;
import cl.mecanicontrol.backend.service.PrediccionTiempoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ia")
@RequiredArgsConstructor
public class PrediccionTiempoController {

    private final PrediccionTiempoService prediccionService;

    /** Genera (o regenera) la predicción IA para una OT. */
    @PostMapping("/predecir-tiempo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrediccionResponseDTO> predecir(@RequestParam UUID otId) {
        return ResponseEntity.ok(prediccionService.predecir(otId));
    }

    /** Devuelve la predicción ya guardada para una OT. */
    @GetMapping("/prediccion/{otId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrediccionResponseDTO> obtener(@PathVariable UUID otId) {
        return ResponseEntity.ok(prediccionService.obtenerPrediccion(otId));
    }
}