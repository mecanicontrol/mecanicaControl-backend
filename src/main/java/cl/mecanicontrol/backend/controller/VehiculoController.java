package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.VehiculoRequestDTO;
import cl.mecanicontrol.backend.dto.VehiculoResponseDTO;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.entity.NivelFidelizacion;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import cl.mecanicontrol.backend.repository.NivelFidelizacionRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.repository.VehiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoRepository vehiculoRepo;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final MarcaVehiculoRepository marcaRepo;
    private final ModeloVehiculoRepository modeloRepo;
    private final NivelFidelizacionRepository nivelRepo;

    public VehiculoController(VehiculoRepository vehiculoRepo,
                               ClienteRepository clienteRepo,
                               UsuarioRepository usuarioRepo,
                               MarcaVehiculoRepository marcaRepo,
                               ModeloVehiculoRepository modeloRepo,
                               NivelFidelizacionRepository nivelRepo) {
        this.vehiculoRepo = vehiculoRepo;
        this.clienteRepo  = clienteRepo;
        this.usuarioRepo  = usuarioRepo;
        this.marcaRepo    = marcaRepo;
        this.modeloRepo   = modeloRepo;
        this.nivelRepo    = nivelRepo;
    }

    @GetMapping("/mis-vehiculos")
    public ResponseEntity<List<VehiculoResponseDTO>> misVehiculos(Authentication auth) {
        Cliente cliente = resolverCliente(auth.getName());
        List<Vehiculo> vehiculos = vehiculoRepo.findByClienteId(cliente.getId());

        Set<UUID> marcaIds = vehiculos.stream()
                .map(Vehiculo::getMarcaVehiculoId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> modeloIds = vehiculos.stream()
                .map(Vehiculo::getModeloVehiculoId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<UUID, String> marcaNombres = marcaRepo.findAllById(marcaIds).stream()
                .collect(Collectors.toMap(MarcaVehiculo::getId, MarcaVehiculo::getNombre));
        Map<UUID, String> modeloNombres = modeloRepo.findAllById(modeloIds).stream()
                .collect(Collectors.toMap(ModeloVehiculo::getId, ModeloVehiculo::getNombre));

        return ResponseEntity.ok(
                vehiculos.stream().map(v -> toDTO(v, marcaNombres, modeloNombres)).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> findById(@PathVariable UUID id) {
        Vehiculo v = vehiculoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado"));
        return ResponseEntity.ok(toDTO(v));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> update(@PathVariable UUID id, @RequestBody VehiculoRequestDTO request) {
        Vehiculo v = vehiculoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado"));

        if (request.patente() != null) v.setPatente(request.patente().toUpperCase().trim());
        if (request.marcaId() != null) v.setMarcaVehiculoId(UUID.fromString(request.marcaId()));
        if (request.modeloId() != null) v.setModeloVehiculoId(UUID.fromString(request.modeloId()));
        if (request.anio() != null) v.setAnio(request.anio().shortValue());
        if (request.kilometraje() != null) v.setKilometrajeIngreso(request.kilometraje());
        if (request.alias() != null) v.setAlias(request.alias());

        vehiculoRepo.save(v);
        return ResponseEntity.ok(toDTO(v));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> crear(@RequestBody VehiculoRequestDTO request,
                                                      Authentication auth) {
        Cliente cliente = resolverCliente(auth.getName());

        Vehiculo v = new Vehiculo();
        v.setClienteId(cliente.getId());
        if (request.patente() != null && !request.patente().isBlank())
            v.setPatente(request.patente().toUpperCase().trim());
        if (request.marcaId() != null && !request.marcaId().isBlank())
            v.setMarcaVehiculoId(UUID.fromString(request.marcaId()));
        if (request.modeloId() != null && !request.modeloId().isBlank())
            v.setModeloVehiculoId(UUID.fromString(request.modeloId()));
        if (request.anio() != null)
            v.setAnio(request.anio().shortValue());
        if (request.kilometraje() != null)
            v.setKilometrajeIngreso(request.kilometraje());
        if (request.alias() != null)
            v.setAlias(request.alias());
        vehiculoRepo.save(v);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(v));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Cliente resolverCliente(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return clienteRepo.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    NivelFidelizacion nivel = nivelRepo.findAll().stream()
                            .findFirst()
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR, "No hay niveles de fidelización configurados"));
                    Cliente nuevo = new Cliente();
                    nuevo.setUsuario(usuario);
                    nuevo.setNivelFidelizacion(nivel);
                    nuevo.setPuntosFidelizacion(0);
                    return clienteRepo.save(nuevo);
                });
    }

    private VehiculoResponseDTO toDTO(Vehiculo v) {
        String marcaNombre  = v.getMarcaVehiculoId()  != null
                ? marcaRepo.findById(v.getMarcaVehiculoId()).map(MarcaVehiculo::getNombre).orElse(null)  : null;
        String modeloNombre = v.getModeloVehiculoId() != null
                ? modeloRepo.findById(v.getModeloVehiculoId()).map(ModeloVehiculo::getNombre).orElse(null) : null;
        return buildDTO(v, marcaNombre, modeloNombre);
    }

    private VehiculoResponseDTO toDTO(Vehiculo v, Map<UUID, String> marcaNombres, Map<UUID, String> modeloNombres) {
        return buildDTO(v,
                v.getMarcaVehiculoId()  != null ? marcaNombres.get(v.getMarcaVehiculoId())  : null,
                v.getModeloVehiculoId() != null ? modeloNombres.get(v.getModeloVehiculoId()) : null);
    }

    private VehiculoResponseDTO buildDTO(Vehiculo v, String marcaNombre, String modeloNombre) {
        return new VehiculoResponseDTO(
                v.getId(),
                v.getPatente(),
                v.getMarcaVehiculoId(),
                marcaNombre,
                v.getModeloVehiculoId(),
                modeloNombre,
                v.getAnio(),
                v.getKilometrajeIngreso(),
                v.getAlias()
        );
    }
}