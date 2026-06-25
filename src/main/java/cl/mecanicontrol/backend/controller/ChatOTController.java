package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.MensajeOT;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.repository.MensajeOTRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatOTController {

    private final MensajeOTRepository mensajeRepo;
    private final OrdenTrabajoRepository otRepo;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/ot/{codigoOt}")
    public ResponseEntity<List<Map<String, Object>>> getMensajes(
            @PathVariable String codigoOt,
            @AuthenticationPrincipal UserDetails userDetails) {

        OrdenTrabajo ot = otRepo.findByCodigoOt(codigoOt)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT no encontrada"));

        List<MensajeOT> mensajes = mensajeRepo.findByOrdenTrabajoIdOrderByCreadoAtAsc(ot.getId());

        List<Map<String, Object>> result = mensajes.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("rol", m.getRol());
            map.put("contenido", m.getContenido());
            map.put("autor", m.getUsuario().getNombre());
            map.put("creadoAt", m.getCreadoAt());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @PostMapping("/ot/{codigoOt}")
    public ResponseEntity<Map<String, Object>> enviarMensaje(
            @PathVariable String codigoOt,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String contenido = body.get("contenido");
        if (contenido == null || contenido.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contenido requerido");

        OrdenTrabajo ot = otRepo.findByCodigoOt(codigoOt)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT no encontrada"));

        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        String rol = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .filter(a -> List.of("CLIENTE", "TECNICO", "ADMIN").contains(a))
                .findFirst().orElse("CLIENTE");

        MensajeOT mensaje = new MensajeOT();
        mensaje.setOrdenTrabajo(ot);
        mensaje.setUsuario(usuario);
        mensaje.setRol(rol);
        mensaje.setContenido(contenido.trim());
        MensajeOT saved = mensajeRepo.save(mensaje);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", saved.getId());
        map.put("rol", saved.getRol());
        map.put("contenido", saved.getContenido());
        map.put("autor", usuario.getNombre());
        map.put("creadoAt", saved.getCreadoAt());

        return ResponseEntity.ok(map);
    }
}