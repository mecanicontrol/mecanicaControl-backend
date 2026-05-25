package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.ot.AvanzarFaseDTO;
import cl.mecanicontrol.backend.dto.ot.OTDetalleDTO;
import cl.mecanicontrol.backend.dto.ot.OTRequestDTO;
import cl.mecanicontrol.backend.dto.ot.OTResponseDTO;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.service.OrdenTrabajoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenTrabajoController {

    private final OrdenTrabajoService otService;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;

    public OrdenTrabajoController(OrdenTrabajoService otService,
                                  UsuarioRepository usuarioRepository,
                                  TecnicoRepository tecnicoRepository){
        this.otService = otService;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OTResponseDTO> crear(@Valid @RequestBody OTRequestDTO dto){
        return ResponseEntity.status(201).body(otService.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OTResponseDTO>> findAll(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) UUID tecnicoId){
        return ResponseEntity.ok(otService.findAll(estado, tecnicoId));
    }

    @GetMapping("/mis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OTResponseDTO>> misOrdenes(
            @AuthenticationPrincipal UserDetails userDetails){
        UUID tecnicoId = resolverTecnicoId(userDetails.getUsername());
        return ResponseEntity.ok(otService.findAll(null, tecnicoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OTDetalleDTO> findById(@PathVariable UUID id){
        return ResponseEntity.ok(otService.findById(id));
    }

    @PutMapping("/{id}/tecnico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OTResponseDTO> asignarTecnico(
            @PathVariable UUID id,
            @RequestParam UUID tecnicoId){
        return ResponseEntity.ok(otService.asignarTecnico(id, tecnicoId));
    }

    @PutMapping("/{id}/fase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public ResponseEntity<OTResponseDTO> avanzarFase(
            @PathVariable UUID id,
            @Valid @RequestBody AvanzarFaseDTO dto){
        return ResponseEntity.ok(otService.avanzarFase(id, dto));
    }

    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OTResponseDTO> cerrar(@PathVariable UUID id){
        return ResponseEntity.ok(otService.cerrar(id));
    }

    private UUID resolverTecnicoId(String email){
        var usuario =  usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return tecnicoRepository.findByIdUsuarioId(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Tecnico no encontrado"))
                .getIdTecnico();
    }
}
