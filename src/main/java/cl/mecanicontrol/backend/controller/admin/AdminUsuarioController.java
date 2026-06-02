package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.dto.usuario.UsuarioResponseDTO;
import cl.mecanicontrol.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@RequestBody Map<String, String> body) {
        UsuarioResponseDTO dto = usuarioService.crearUsuarioAdmin(
                body.get("nombre"),
                body.get("apellido"),
                body.get("email"),
                body.get("password"),
                body.getOrDefault("rolNombre", "CLIENTE"),
                body.get("telefono")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        UsuarioResponseDTO dto = usuarioService.actualizarUsuarioAdmin(
                id,
                body.get("nombre"),
                body.get("apellido"),
                body.get("email"),
                body.get("rolNombre")
        );
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Void> toggle(@PathVariable UUID id) {
        usuarioService.toggleActivo(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        usuarioService.cambiarPasswordAdmin(id, body.get("nuevaPassword"));
        return ResponseEntity.ok().build();
    }
}
