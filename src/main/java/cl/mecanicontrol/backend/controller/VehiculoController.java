package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.VehiculoRequestDTO;
import cl.mecanicontrol.backend.dto.VehiculoResponseDTO;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.entity.NivelFidelizacion;
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
import java.util.UUID;

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
        List<VehiculoResponseDTO> dtos = vehiculoRepo.findByClienteId(cliente.getId())
                .stream().map(this::toDTO).toList();
        return ResponseEntity.ok(dtos);
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
        if (request.patente() != null && !request.patente().isBlank()) {
            v.setPatente(request.patente().toUpperCase().trim());
        }
        if (request.marcaId() != null && !request.marcaId().isBlank()) {
            v.setMarcaVehiculoId(UUID.fromString(request.marcaId()));
        }
        if (request.modeloId() != null && !request.modeloId().isBlank()) {
            v.setModeloVehiculoId(UUID.fromString(request.modeloId()));
        }
        if (request.anio() != null) {
            v.setAnio(request.anio().shortValue());
        }
        if (request.kilometraje() != null) {
            v.setKilometrajeIngreso(request.kilometraje());
        }
        if (request.alias() != null) {
            v.setAlias(request.alias());
        }
        vehiculoRepo.save(v);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(v));
    }

    private Cliente resolverCliente(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return clienteRepo.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    // Usuario registrado por flujo antiguo — crear cliente automáticamente
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
        String marcaNombre = null;
        String modeloNombre = null;
        if (v.getMarcaVehiculoId() != null) {
            marcaNombre = marcaRepo.findById(v.getMarcaVehiculoId())
                    .map(MarcaVehiculo::getNombre).orElse(null);
        }
        if (v.getModeloVehiculoId() != null) {
            modeloNombre = modeloRepo.findById(v.getModeloVehiculoId())
                    .map(ModeloVehiculo::getNombre).orElse(null);
        }
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
