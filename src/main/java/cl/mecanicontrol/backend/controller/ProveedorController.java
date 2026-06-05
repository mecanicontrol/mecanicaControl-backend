package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.Proveedor;
import cl.mecanicontrol.backend.service.ProveedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Proveedor>> findAll() {
        return ResponseEntity.ok(proveedorService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Proveedor> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(proveedorService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Proveedor> crear(@RequestBody Proveedor proveedor) {
        return ResponseEntity.status(201).body(proveedorService.crear(proveedor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Proveedor> update(@PathVariable UUID id, @RequestBody Proveedor proveedor) {
        return ResponseEntity.ok(proveedorService.update(id, proveedor));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleActivo(@PathVariable UUID id) {
        proveedorService.toggleActivo(id);
        return ResponseEntity.ok().build();
    }
}
