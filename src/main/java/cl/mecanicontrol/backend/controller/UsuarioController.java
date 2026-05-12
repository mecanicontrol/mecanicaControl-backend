package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.usuario.PasswordUpdateDTO;
import cl.mecanicontrol.backend.dto.usuario.PerfilUpdateDTO;
import cl.mecanicontrol.backend.dto.usuario.UsuarioResponseDTO;
import cl.mecanicontrol.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final cl.mecanicontrol.backend.repository.UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioService usuarioService, cl.mecanicontrol.backend.repository.UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getMiPerfil(
            @AuthenticationPrincipal UserDetails userDetails){
        UUID usuarioId = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
        return ResponseEntity.ok(usuarioService.getMiPerfil(usuarioId));
    }

    @PutMapping("/me/perfil")
    public ResponseEntity<Void> updatePerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerfilUpdateDTO dto){
        UUID usuarioId = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
        usuarioService.updatePerfil(usuarioId, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateDTO dto){
        UUID usuarioId = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
        usuarioService.cambiarPassword(usuarioId, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll(){
        return ResponseEntity.ok(usuarioService.findAll());
    }
}
