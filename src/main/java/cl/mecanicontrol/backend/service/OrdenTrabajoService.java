package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.ot.*;
import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrdenTrabajoService {
    private final OrdenTrabajoRepository otRepository;
    private final FaseVehiculoRepository faseVehiculoRepository;
    private final FaseRepository faseRepository;
    private final TecnicoRepository tecnicoRepository;
    private final AgendamientoRepository agendamientoRepository;
    private final EstadoOtRepository estadoOtRepository;
    private final ClienteRepository clienteRepository;

    public OrdenTrabajoService(OrdenTrabajoRepository otRepository,
                               FaseVehiculoRepository faseVehiculoRepository,
                               FaseRepository faseRepository,
                               TecnicoRepository tecnicoRepository,
                               AgendamientoRepository agendamientoRepository,
                               EstadoOtRepository estadoOtRepository,
                               ClienteRepository clienteRepository){
        this.otRepository = otRepository;
        this.faseVehiculoRepository = faseVehiculoRepository;
        this.faseRepository = faseRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.agendamientoRepository = agendamientoRepository;
        this.estadoOtRepository = estadoOtRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public OTResponseDTO crear(OTRequestDTO dto){
        Agendamiento agendamiento = agendamientoRepository.findById(dto.agendamientoId())
                .orElseThrow(() -> new RuntimeException("Agendamiento no encontrado"));

        EstadoOt estadoInicial = estadoOtRepository.findByNombre("ACTIVA")
                .orElseThrow(() -> new RuntimeException("Estado OT no encontrada"));

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setAgendamiento(agendamiento);
        ot.setEstadoOt(estadoInicial);
        ot.setCodigoOt(generarCodigo());
        ot.setFechaInicio(LocalDateTime.now());
        // costoManoObra = suma de precioBase de todos los servicios del agendamiento
        BigDecimal costoServicios = agendamiento.getServicios() != null && !agendamiento.getServicios().isEmpty()
            ? agendamiento.getServicios().stream()
                .map(s -> s.getPrecioBase() != null ? s.getPrecioBase() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
            : (agendamiento.getServicio() != null && agendamiento.getServicio().getPrecioBase() != null
                ? agendamiento.getServicio().getPrecioBase()
                : BigDecimal.ZERO);
        ot.setCostoManoObra(costoServicios);
        ot.setCostoRepuestos(BigDecimal.ZERO);

        if (dto.tecnicoId() != null){
            Tecnico tecnico = tecnicoRepository.findById(dto.tecnicoId())
                    .orElseThrow(() -> new RuntimeException("Tecnico no encontrado"));
            ot.setTecnico(tecnico);
        }
        otRepository.save(ot);
        crearFasesIniciales(ot);

        Map<UUID, String> clienteNombres = buildClienteNombresMap(List.of(ot));
        return toDTO(ot, clienteNombres);
    }

    @Transactional
    public OTResponseDTO asignarTecnico(UUID otId, UUID tecnicoId){
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow(() -> new RuntimeException("OT no encontrada"));
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElseThrow(() -> new RuntimeException("Tecnico no encontrado"));
        ot.setTecnico(tecnico);
        OrdenTrabajo saved = otRepository.save(ot);
        Map<UUID, String> clienteNombres = buildClienteNombresMap(List.of(saved));
        return toDTO(saved, clienteNombres);
    }

    @Transactional
    public OTResponseDTO avanzarFase(UUID otId, AvanzarFaseDTO dto) {
        OrdenTrabajo ot = otRepository.findById(otId)
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));

        List<FaseVehiculo> fases = faseVehiculoRepository.findByOrdenTrabajoId(otId)
                .stream()
                .sorted((a, b) -> a.getFase().getOrden().compareTo(b.getFase().getOrden()))
                .toList();

        FaseVehiculo faseActual = fases.stream()
                .filter(f -> f.getFinAt() == null)
                .min((a, b) -> a.getFase().getOrden().compareTo(b.getFase().getOrden()))
                .orElseThrow(() -> new RuntimeException("No existe fase activa"));

        if (dto.checklistJson() == null || dto.checklistJson().isBlank()) {
            throw new RuntimeException("El checklist es obligatorio para completar la fase");
        }

        faseActual.setFinAt(LocalDateTime.now());
        faseActual.setChecklistJson(dto.checklistJson());
        faseActual.setObservaciones(dto.observaciones());
        faseVehiculoRepository.save(faseActual);

        Map<UUID, String> clienteNombres = buildClienteNombresMap(List.of(ot));
        return toDTO(ot, clienteNombres);
    }

    @Transactional
    public OTResponseDTO cerrar(UUID otId){
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow(() -> new RuntimeException("OT no encontrada"));

        EstadoOt estadoCerrada = estadoOtRepository.findByNombre("COMPLETADA")
                .orElseThrow(() -> new RuntimeException("Estado OT no encontrado"));

        ot.setEstadoOt(estadoCerrada);
        ot.setFechaCierre(LocalDateTime.now());

        OrdenTrabajo saved = otRepository.save(ot);
        Map<UUID, String> clienteNombres = buildClienteNombresMap(List.of(saved));
        return toDTO(saved, clienteNombres);
    }

    public OTDetalleDTO findById(UUID otId){
        OrdenTrabajo ot = otRepository.findById(otId)
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));
        Map<UUID, String> clienteNombres = buildClienteNombresMap(List.of(ot));
        return toDetalleDTO(ot, clienteNombres);
    }

    public OTDetalleDTO findByCodigoOt(String codigoOt){
        OrdenTrabajo ot = otRepository.findByCodigoOtEager(codigoOt)
                .orElseThrow(() -> new RuntimeException("OT no encontrada: " + codigoOt));
        Map<UUID, String> clienteNombres = buildClienteNombresMap(List.of(ot));
        return toDetalleDTO(ot, clienteNombres);
    }

    public List<OTResponseDTO> findAll(String estado, UUID tecnicoId){
        List<OrdenTrabajo> ots;
        if (estado != null){
            ots = otRepository.findByEstadoOtNombreEager(estado);
        } else if (tecnicoId != null){
            ots = otRepository.findByTecnicoIdEager(tecnicoId);
        } else {
            ots = otRepository.findAllEager();
        }
        Map<UUID, String> clienteNombres = buildClienteNombresMap(ots);
        return ots.stream().map(ot -> toDTO(ot, clienteNombres)).toList();
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Map<UUID, String> buildClienteNombresMap(List<OrdenTrabajo> ots) {
        Set<UUID> clienteIds = ots.stream()
                .filter(ot -> ot.getAgendamiento() != null && ot.getAgendamiento().getIdVehiculo() != null)
                .map(ot -> ot.getAgendamiento().getIdVehiculo().getClienteId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (clienteIds.isEmpty()) return Map.of();
        return clienteRepository.findAllById(clienteIds)
                .stream()
                .filter(c -> c.getUsuario() != null)
                .collect(Collectors.toMap(
                        Cliente::getId,
                        c -> c.getUsuario().getNombre() + " " + c.getUsuario().getApellido()
                ));
    }

    private String resolverNombreCliente(OrdenTrabajo ot, Map<UUID, String> clienteNombres) {
        try {
            UUID clienteId = ot.getAgendamiento().getIdVehiculo().getClienteId();
            return clienteId != null ? clienteNombres.get(clienteId) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private OTResponseDTO toDTO(OrdenTrabajo ot, Map<UUID, String> clienteNombres){
        String vehiculoPatente = null;
        String nombreServicio  = null;
        if (ot.getAgendamiento() != null) {
            if (ot.getAgendamiento().getIdVehiculo() != null)
                vehiculoPatente = ot.getAgendamiento().getIdVehiculo().getPatente();
            if (ot.getAgendamiento().getServicio() != null)
                nombreServicio = ot.getAgendamiento().getServicio().getNombreServicio();
        }

        String nombreTecnico = ot.getTecnico() != null
                ? ot.getTecnico().getIdUsuario().getNombre() + " " + ot.getTecnico().getIdUsuario().getApellido()
                : null;

        return new OTResponseDTO(
                ot.getId(),
                ot.getCodigoOt(),
                ot.getEstadoOt().getNombre(),
                resolverNombreCliente(ot, clienteNombres),
                vehiculoPatente,
                nombreTecnico,
                nombreServicio,
                ot.getCostoManoObra(),
                ot.getCostoRepuestos(),
                ot.getTotal(),
                ot.getFechaInicio(),
                ot.getFechaCierre()
        );
    }

    private OTDetalleDTO toDetalleDTO(OrdenTrabajo ot, Map<UUID, String> clienteNombres) {
        String vehiculoPatente     = null;
        String vehiculoDescripcion = null;
        String nombreServicio      = null;
        if (ot.getAgendamiento() != null) {
            Vehiculo v = ot.getAgendamiento().getIdVehiculo();
            if (v != null) {
                vehiculoPatente = v.getPatente();
                if (v.getAnio() != null)
                    vehiculoDescripcion = String.valueOf(v.getAnio());
            }
            if (ot.getAgendamiento().getServicio() != null)
                nombreServicio = ot.getAgendamiento().getServicio().getNombreServicio();
        }

        String nombreTecnico = ot.getTecnico() != null
                ? ot.getTecnico().getIdUsuario().getNombre() + " " + ot.getTecnico().getIdUsuario().getApellido()
                : null;

        List<FaseDTO> fases = faseVehiculoRepository.findByOrdenTrabajoId(ot.getId())
                .stream().map(this::toFaseDTO).toList();

        return new OTDetalleDTO(
                ot.getId(),
                ot.getCodigoOt(),
                ot.getEstadoOt().getNombre(),
                resolverNombreCliente(ot, clienteNombres),
                vehiculoPatente,
                vehiculoDescripcion,
                nombreTecnico,
                nombreServicio,
                ot.getDiagnostico(),
                ot.getCostoManoObra(),
                ot.getCostoRepuestos(),
                ot.getTotal(),
                ot.getFechaInicio(),
                ot.getFechaCierre(),
                fases
        );
    }

    private void crearFasesIniciales(OrdenTrabajo ot) {
        List<Fase> fases = faseRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getOrden().compareTo(b.getOrden()))
                .toList();

        for (Fase fase : fases) {
            FaseVehiculo fv = new FaseVehiculo();
            fv.setOrdenTrabajo(ot);
            fv.setFase(fase);
            fv.setInicioAt(LocalDateTime.now());
            faseVehiculoRepository.save(fv);
        }
    }

    private String generarCodigo(){
        int anio = LocalDate.now().getYear();
        long count = otRepository.count() + 1;
        return String.format("OT-%d-%04d", anio, count);
    }

    private FaseDTO toFaseDTO(FaseVehiculo fv){
        Long duracion = null;
        if (fv.getInicioAt() != null && fv.getFinAt() != null){
            duracion = ChronoUnit.MINUTES.between(fv.getInicioAt(), fv.getFinAt());
        }
        return new FaseDTO(
                fv.getId(),
                fv.getFase().getNombre(),
                (int) fv.getFase().getOrden(),
                fv.getChecklistJson(),
                fv.getObservaciones(),
                fv.getImagenes(),
                fv.getInicioAt(),
                fv.getFinAt(),
                duracion,
                fv.getAprobacionEstado(),
                fv.getAprobacionNota()
        );
    }
}