package cl.mecanicontrol.backend.controller;

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

import cl.mecanicontrol.backend.entity.CategoriaServicio;
import cl.mecanicontrol.backend.repository.CategoriaServicioRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/categorias-servicio")
@RequiredArgsConstructor
public class CategoriaServicioController {

    private final CategoriaServicioRepository repo;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoriaDTO>> findAll() {
        return ResponseEntity.ok(repo.findAll().stream().map(this::toDTO).toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaDTO> crear(@RequestBody CategoriaBody req) {
        CategoriaServicio c = new CategoriaServicio();
        c.setNombreCategoriaServicio(req.nombre());
        c.setDescripcionCategoriaService(req.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(repo.save(c)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaDTO> actualizar(@PathVariable UUID id, @RequestBody CategoriaBody req) {
        CategoriaServicio c = repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        if (req.nombre() != null)      c.setNombreCategoriaServicio(req.nombre());
        if (req.descripcion() != null) c.setDescripcionCategoriaService(req.descripcion());
        return ResponseEntity.ok(toDTO(repo.save(c)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private record CategoriaBody(String nombre, String descripcion) {}
    private record CategoriaDTO(UUID id, String nombre, String descripcion) {}

    private CategoriaDTO toDTO(CategoriaServicio c) {
        return new CategoriaDTO(c.getId_categoria_servicio(), c.getNombreCategoriaServicio(), c.getDescripcionCategoriaService());
    }
}