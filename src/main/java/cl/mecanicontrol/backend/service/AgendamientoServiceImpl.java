package cl.mecanicontrol.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.mecanicontrol.backend.dto.AgendamientoRequestDTO;
import cl.mecanicontrol.backend.dto.AgendamientoResponsedDTO;
import cl.mecanicontrol.backend.dto.DisponibilidadSlotDTO;
import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.entity.EstadoAgendamiento;
import cl.mecanicontrol.backend.entity.ServicioCatalogo;
import cl.mecanicontrol.backend.entity.Tecnico;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.AgendamientoRepository;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.EstadoAgendamientoRepository;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import cl.mecanicontrol.backend.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendamientoServiceImpl implements AgendamientoService {

    private final AgendamientoRepository agendamientoRepo;
    private final VehiculoRepository vehiculoRepo;
    private final ServicioCatalogoRepository servicioRepo;
    private final EstadoAgendamientoRepository estadoRepo;
    private final TecnicoRepository tecnicoRepo;
    private final ClienteRepository clienteRepo;

    @Override
    public AgendamientoResponsedDTO crear(AgendamientoRequestDTO request) {
        Vehiculo vehiculo = resolverVehiculo(request);

        ServicioCatalogo servicio = servicioRepo.findById(request.idServicio())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        EstadoAgendamiento estadoPendiente = estadoRepo.findByNombre("PENDIENTE")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado PENDIENTE no configurado"));

        LocalDateTime fechaFin = request.fechaInicio().plusMinutes(
            servicio.getDuracion() != null ? servicio.getDuracion() : 60
        );

        Agendamiento agendamiento = new Agendamiento();
        agendamiento.setIdVehiculo(vehiculo);
        agendamiento.setServicio(servicio);
        agendamiento.setIdEstadoAgendamiento(estadoPendiente);
        agendamiento.setFechaInicio(request.fechaInicio());
        agendamiento.setFechaFin(fechaFin);
        agendamiento.setNotaCliente(request.notaCliente());
        agendamiento.setCreatedAtAgendamiento(LocalDateTime.now());

        Agendamiento guardado = agendamientoRepo.save(agendamiento);
        log.info("Agendamiento creado: {}", guardado.getIdAgendamiento());

        return toDTO(guardado);
    }

    @Override
    public List<AgendamientoResponsedDTO> findAll(String estado, LocalDate fecha) {
        LocalDateTime desde = fecha != null ? fecha.atStartOfDay() : null;
        LocalDateTime hasta = fecha != null ? fecha.atTime(LocalTime.MAX) : null;
        return agendamientoRepo.findAllFiltrado(estado, desde, hasta)
            .stream().map(this::toDTO).toList();
    }

    @Override
    public List<AgendamientoResponsedDTO> findByCliente(UUID clienteId) {
        return agendamientoRepo.findByClienteId(clienteId)
            .stream().map(this::toDTO).toList();
    }

    @Override
    public List<AgendamientoResponsedDTO> findByTecnico(UUID tecnicoId) {
        return agendamientoRepo.findByTecnicoId(tecnicoId)
            .stream().map(this::toDTO).toList();
    }

    @Override
    public AgendamientoResponsedDTO confirmar(UUID id, UUID tecnicoId) {
        Agendamiento agendamiento = agendamientoRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamiento no encontrado"));

        if (!"PENDIENTE".equals(agendamiento.getIdEstadoAgendamiento().getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden confirmar agendamientos en estado PENDIENTE");
        }

        Tecnico tecnico = tecnicoRepo.findById(tecnicoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado"));

        if (agendamientoRepo.existeConflictoHorario(tecnicoId, agendamiento.getFechaInicio(), agendamiento.getFechaFin())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El técnico ya tiene un agendamiento en ese horario");
        }

        EstadoAgendamiento estadoConfirmado = estadoRepo.findByNombre("CONFIRMADO")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado CONFIRMADO no configurado"));

        agendamiento.setIdTecnico(tecnico);
        agendamiento.setIdEstadoAgendamiento(estadoConfirmado);
        agendamiento.setPrecioAcordado(agendamiento.getServicio().getPrecioBase());

        Agendamiento guardado = agendamientoRepo.save(agendamiento);
        log.info("Agendamiento {} confirmado con técnico {}", id, tecnicoId);

        return toDTO(guardado);
    }

    @Override
    public AgendamientoResponsedDTO cancelar(UUID id) {
        Agendamiento agendamiento = agendamientoRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamiento no encontrado"));

        String estadoActual = agendamiento.getIdEstadoAgendamiento().getNombre();
        if ("COMPLETADO".equals(estadoActual) || "CANCELADO".equals(estadoActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cancelar un agendamiento " + estadoActual);
        }

        EstadoAgendamiento estadoCancelado = estadoRepo.findByNombre("CANCELADO")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Estado CANCELADO no configurado"));

        agendamiento.setIdEstadoAgendamiento(estadoCancelado);
        Agendamiento guardado = agendamientoRepo.save(agendamiento);
        log.info("Agendamiento {} cancelado", id);

        return toDTO(guardado);
    }

    private static final int CAPACIDAD_MAXIMA_DIA = 15;
    private static final LocalTime HORA_APERTURA   = LocalTime.of(9, 0);
    private static final LocalTime HORA_CIERRE     = LocalTime.of(16, 0);

    @Override
    public List<DisponibilidadSlotDTO> getDisponibilidad(LocalDate fecha, UUID servicioId) {
        ServicioCatalogo servicio = servicioRepo.findById(servicioId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        int duracionMinutos = servicio.getDuracion() != null ? servicio.getDuracion() : 60;

        List<DisponibilidadSlotDTO> slots = new ArrayList<>();
        LocalDateTime slotInicio = fecha.atTime(HORA_APERTURA);

        // Slots de 1 hora fija — cualquier servicio puede iniciar antes del cierre.
        // duracionMinutos solo define el fechaHoraFin para el cálculo de capacidad.
        while (slotInicio.toLocalTime().isBefore(HORA_CIERRE)) {
            LocalDateTime slotFin = slotInicio.plusMinutes(duracionMinutos);

            long vehiculosEnTaller = agendamientoRepo.countVehiculosEnTallerEnSlot(slotInicio);
            boolean disponible = vehiculosEnTaller < CAPACIDAD_MAXIMA_DIA;

            slots.add(new DisponibilidadSlotDTO(slotInicio.toString(), slotFin.toString(), disponible, null, null));
            slotInicio = slotInicio.plusHours(1);
        }

        return slots;
    }

    private Vehiculo resolverVehiculo(AgendamientoRequestDTO request) {
        // Si viene idVehiculo lo usamos directamente
        if (request.idVehiculo() != null) {
            return vehiculoRepo.findById(request.idVehiculo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado"));
        }
        // Si viene patente buscamos por ella
        if (request.patente() != null && !request.patente().isBlank()) {
            return vehiculoRepo.findByPatente(request.patente().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró un vehículo con patente " + request.patente() +
                    ". Por favor regístrate o inicia sesión para guardarlo."));
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere idVehiculo o patente");
    }

    private AgendamientoResponsedDTO toDTO(Agendamiento a) {
        String nombreCliente = null;
        String emailCliente = null;
        if (a.getIdVehiculo() != null && a.getIdVehiculo().getClienteId() != null) {
            clienteRepo.findById(a.getIdVehiculo().getClienteId()).ifPresent(c -> {});
            // Se resuelve abajo con variable local
        }

        Cliente cliente = (a.getIdVehiculo() != null && a.getIdVehiculo().getClienteId() != null)
            ? clienteRepo.findById(a.getIdVehiculo().getClienteId()).orElse(null)
            : null;

        if (cliente != null && cliente.getUsuario() != null) {
            nombreCliente = cliente.getUsuario().getNombre() + " " + cliente.getUsuario().getApellido();
            emailCliente = cliente.getUsuario().getEmail();
        }

        String nombreTecnico = null;
        if (a.getIdTecnico() != null && a.getIdTecnico().getIdUsuario() != null) {
            nombreTecnico = a.getIdTecnico().getIdUsuario().getNombre() + " " + a.getIdTecnico().getIdUsuario().getApellido();
        }

        return new AgendamientoResponsedDTO(
            a.getIdAgendamiento(),
            a.getIdVehiculo() != null ? a.getIdVehiculo().getId() : null,
            a.getServicio() != null ? a.getServicio().getId() : null,
            a.getIdEstadoAgendamiento(),
            a.getFechaInicio(),
            a.getFechaFin(),
            a.getPrecioAcordado() != null ? a.getPrecioAcordado().intValue() : null,
            nombreCliente,
            emailCliente,
            null,
            a.getIdVehiculo() != null ? a.getIdVehiculo().getPatente() : null,
            null,
            null,
            a.getIdVehiculo() != null && a.getIdVehiculo().getAnio() != null ? a.getIdVehiculo().getAnio().intValue() : null,
            nombreTecnico,
            a.getServicio() != null ? a.getServicio().getNombreServicio() : null,
            a.getCreatedAtAgendamiento()
        );
    }
}
