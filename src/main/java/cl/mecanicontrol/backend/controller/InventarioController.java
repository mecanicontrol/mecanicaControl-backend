package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.inventario.MovimientoRequestDTO;
import cl.mecanicontrol.backend.dto.inventario.ProductoRequestDTO;
import cl.mecanicontrol.backend.dto.inventario.ProductoResponseDTO;
import cl.mecanicontrol.backend.entity.MovimientoInventario;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class InventarioController {

    private final InventarioService inventarioService;
    private final UsuarioRepository usuarioRepository;

    public InventarioController(InventarioService inventarioService, UsuarioRepository usuarioRepository) {
        this.inventarioService = inventarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/api/productos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<List<ProductoResponseDTO>> findAll() {
        return ResponseEntity.ok(inventarioService.findAll());
    }

    @GetMapping("/api/productos/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ProductoResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventarioService.findById(id));
    }

    @PostMapping("/api/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(201).body(inventarioService.crear(dto));
    }

    @PutMapping("/api/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(inventarioService.update(id, dto));
    }

    @GetMapping("/api/inventario/alertas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<List<ProductoResponseDTO>> getAlertas(){
        return ResponseEntity.ok(inventarioService.findAlertas());
    }

    @PostMapping("/api/inventario/movimientos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR') or hasRole('TECNICO')")
    public ResponseEntity<Void> registrarMovimiento(@Valid @RequestBody MovimientoRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        UUID usuarioId = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")).getId();
        inventarioService.registrarMovimiento(dto, usuarioId);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/api/inventario/historial/{productoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<List<MovimientoInventario>> getHistorial(@PathVariable UUID productoId){
        return ResponseEntity.ok(inventarioService.getHistorialProducto(productoId));

    }
}
