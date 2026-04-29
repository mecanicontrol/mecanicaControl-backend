package cl.mecanicontrol.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.dto.ServicioCatalogoDTO;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioCatalogoController {

    private final ServicioCatalogoRepository servicioRepo;

    @GetMapping
    public ResponseEntity<List<ServicioCatalogoDTO>> findActivos() {
        List<ServicioCatalogoDTO> servicios = servicioRepo.findByServicioActivoTrue()
            .stream()
            .map(s -> new ServicioCatalogoDTO(
                s.getId(),
                s.getNombreServicio(),
                s.getDescripcionServicio(),
                s.getDuracion(),
                s.getPrecioBase(),
                s.getCategoriaServicio() != null ? s.getCategoriaServicio().getNombreCategoriaServicio() : null
            ))
            .toList();
        return ResponseEntity.ok(servicios);
    }
}
