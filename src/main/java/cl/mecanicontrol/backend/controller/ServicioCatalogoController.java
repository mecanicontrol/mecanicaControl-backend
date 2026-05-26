package cl.mecanicontrol.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.mecanicontrol.backend.dto.ServicioCatalogoDTO;
import cl.mecanicontrol.backend.entity.CategoriaServicio;
import cl.mecanicontrol.backend.entity.ServicioCatalogo;
import cl.mecanicontrol.backend.repository.CategoriaServicioRepository;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioCatalogoController {

    private final ServicioCatalogoRepository servicioRepo;
    private final CategoriaServicioRepository categoriaRepo;

    @GetMapping
    public ResponseEntity<List<ServicioCatalogoDTO>> findActivos() {
        return ResponseEntity.ok(toList(servicioRepo.findByServicioActivoTrue()));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServicioCatalogoDTO>> findTodos() {
        return ResponseEntity.ok(toList(servicioRepo.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioCatalogoDTO> crear(@RequestBody ServicioRequestBody req) {
        CategoriaServicio cat = categoriaRepo.findById(req.categoriaId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        ServicioCatalogo s = new ServicioCatalogo();
        s.setNombreServicio(req.nombre());
        s.setDescripcionServicio(req.descripcion());
        s.setDuracion(req.duracionMinutos());
        s.setPrecioBase(req.precioBase() != null ? req.precioBase() : BigDecimal.ZERO);
        s.setCategoriaServicio(cat);
        s.setServicioActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(servicioRepo.save(s)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicioCatalogoDTO> actualizar(@PathVariable UUID id, @RequestBody ServicioRequestBody req) {
        ServicioCatalogo s = servicioRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
        if (req.nombre() != null)          s.setNombreServicio(req.nombre());
        if (req.descripcion() != null)     s.setDescripcionServicio(req.descripcion());
        if (req.duracionMinutos() != null) s.setDuracion(req.duracionMinutos());
        if (req.precioBase() != null)      s.setPrecioBase(req.precioBase());
        if (req.categoriaId() != null) {
            CategoriaServicio cat = categoriaRepo.findById(req.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
            s.setCategoriaServicio(cat);
        }
        if (req.activo() != null) s.setServicioActivo(req.activo());
        return ResponseEntity.ok(toDTO(servicioRepo.save(s)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        ServicioCatalogo s = servicioRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
        s.setServicioActivo(false);
        servicioRepo.save(s);
        return ResponseEntity.noContent().build();
    }

    private record ServicioRequestBody(
        String nombre, String descripcion, Integer duracionMinutos,
        BigDecimal precioBase, UUID categoriaId, Boolean activo
    ) {}

    private List<ServicioCatalogoDTO> toList(List<ServicioCatalogo> list) {
        return list.stream().map(this::toDTO).toList();
    }

    private ServicioCatalogoDTO toDTO(ServicioCatalogo s) {
        return new ServicioCatalogoDTO(
            s.getId(),
            s.getNombreServicio(),
            s.getDescripcionServicio(),
            s.getDuracion(),
            s.getPrecioBase(),
            s.getCategoriaServicio() != null ? s.getCategoriaServicio().getNombreCategoriaServicio() : null
        );
    }
}