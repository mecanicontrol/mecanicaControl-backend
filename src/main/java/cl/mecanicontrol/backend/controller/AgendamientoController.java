package cl.mecanicontrol.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.dto.AgendamientoRequestDTO;
import cl.mecanicontrol.backend.dto.AgendamientoResponsedDTO;
import cl.mecanicontrol.backend.dto.ConfirmarAgendamientoDTO;
import cl.mecanicontrol.backend.dto.DisponibilidadSlotDTO;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.service.AgendamientoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgendamientoController {

    private final AgendamientoService agendamientoService;
    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final TecnicoRepository tecnicoRepo;

    // POST /api/agendamientos — público, cliente agenda sin necesidad de JWT
    @PostMapping("/agendamientos")
    public ResponseEntity<AgendamientoResponsedDTO> crear(@RequestBody AgendamientoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamientoService.crear(request));
    }

    // GET /api/agendamientos — solo ADMIN
    @GetMapping("/agendamientos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgendamientoResponsedDTO>> findAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(agendamientoService.findAll(estado, fecha));
    }

    // GET /api/agendamientos/mis — cliente logueado ve sus propios agendamientos
    @GetMapping("/agendamientos/mis")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<AgendamientoResponsedDTO>> misAgendamientos(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID clienteId = resolverClienteId(userDetails.getUsername());
        return ResponseEntity.ok(agendamientoService.findByCliente(clienteId));
    }

    // GET /api/agendamientos/tecnico — técnico logueado ve sus agendamientos
    @GetMapping("/agendamientos/tecnico")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<List<AgendamientoResponsedDTO>> agendamientosTecnico(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID tecnicoId = resolverTecnicoId(userDetails.getUsername());
        return ResponseEntity.ok(agendamientoService.findByTecnico(tecnicoId));
    }

    // PUT /api/agendamientos/{id}/confirmar — admin asigna técnico y confirma
    @PutMapping("/agendamientos/{id}/confirmar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendamientoResponsedDTO> confirmar(
            @PathVariable UUID id,
            @RequestBody ConfirmarAgendamientoDTO body) {
        return ResponseEntity.ok(agendamientoService.confirmar(id, body.tecnicoId()));
    }

    // PUT /api/agendamientos/{id}/cancelar — admin o cliente
    @PutMapping("/agendamientos/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENTE')")
    public ResponseEntity<AgendamientoResponsedDTO> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(agendamientoService.cancelar(id));
    }

    // GET /api/disponibilidad — público, calendario del frontend
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadSlotDTO>> getDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam UUID servicioId) {
        return ResponseEntity.ok(agendamientoService.getDisponibilidad(fecha, servicioId));
    }

    private UUID resolverClienteId(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return clienteRepo.findByUsuarioId(usuario.getId())
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"))
            .getId();
    }

    private UUID resolverTecnicoId(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return tecnicoRepo.findAll().stream()
            .filter(t -> t.getIdUsuario() != null && t.getIdUsuario().getId().equals(usuario.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Técnico no encontrado"))
            .getIdTecnico();
    }
}
