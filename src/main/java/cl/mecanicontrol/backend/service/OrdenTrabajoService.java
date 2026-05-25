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
import java.util.UUID;

@Service
public class OrdenTrabajoService {
    private final OrdenTrabajoRepository otRepository;
    private final FaseVehiculoRepository faseVehiculoRepository;
    private final FaseRepository faseRepository;
    private final TecnicoRepository tecnicoRepository;
    private final AgendamientoRepository agendamientoRepository;
    private final EstadoOtRepository estadoOtRepository;

    public OrdenTrabajoService(OrdenTrabajoRepository otRepository,
                               FaseVehiculoRepository faseVehiculoRepository,
                               FaseRepository faseRepository,
                               TecnicoRepository tecnicoRepository,
                               AgendamientoRepository agendamientoRepository,
                               EstadoOtRepository estadoOtRepository){
        this.otRepository = otRepository;
        this.faseVehiculoRepository = faseVehiculoRepository;
        this.faseRepository = faseRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.agendamientoRepository = agendamientoRepository;
        this.estadoOtRepository = estadoOtRepository;
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
        ot.setCostoManoObra(BigDecimal.ZERO);
        ot.setCostoRepuestos(BigDecimal.ZERO);

        if (dto.tecnicoId() != null){
            Tecnico tecnico = tecnicoRepository.findById(dto.tecnicoId())
                    .orElseThrow(() -> new RuntimeException("Tecnico no encontrado"));
            ot.setTecnico(tecnico);
        }
        otRepository.save(ot);
        crearFasesIniciales(ot);

        return toDTO(ot);
    }

    @Transactional
    public OTResponseDTO asignarTecnico(UUID otId, UUID tecnicoId){
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow(() -> new RuntimeException("OT no encontrada"));

        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElseThrow(() -> new RuntimeException("Tecnico no encontrado"));
        ot.setTecnico(tecnico);
        return toDTO(otRepository.save(ot));
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

        faseActual.setFinAt(LocalDateTime.now());
        faseActual.setChecklistJson(dto.checklistJson());
        faseActual.setObservaciones(dto.observaciones());
        faseVehiculoRepository.save(faseActual);

        return toDTO(ot);
    }

    @Transactional
    public OTResponseDTO cerrar(UUID otId){
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow(() -> new RuntimeException("OT no encontrada"));

        EstadoOt estadoCerrada = estadoOtRepository.findByNombre("COMPLETADA")
                .orElseThrow(() -> new RuntimeException("Estado OT no encontrado"));

        ot.setEstadoOt(estadoCerrada);
        ot.setFechaCierre(LocalDateTime.now());

        return toDTO(otRepository.save(ot));
    }

    public OTDetalleDTO findById(UUID otId){
        OrdenTrabajo ot = otRepository.findById(otId).orElseThrow(() -> new RuntimeException("OT no encontrada"));

        List<FaseDTO> fases = faseVehiculoRepository.findByOrdenTrabajoId(otId)
                .stream().map(this::toFaseDTO).toList();

        return new OTDetalleDTO(toDTO(ot), fases, List.of());
    }

    public List<OTResponseDTO> findAll(String estado, UUID tecnicoId){
        if (estado != null){
            return otRepository.findByEstadoOtNombre(estado).stream().map(this::toDTO).toList();
        }
        if (tecnicoId != null){
            return otRepository.findByTecnicoIdTecnico(tecnicoId).stream().map(this::toDTO).toList();
        }
        return otRepository.findAll().stream().map(this::toDTO).toList();
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

    private OTResponseDTO toDTO(OrdenTrabajo ot){
        String vehiculo = ot.getAgendamiento() != null ?
                ot.getAgendamiento().getIdVehiculo().getPatente(): "N/A";
        String cliente = ot.getAgendamiento() != null ?
                ot.getAgendamiento().getIdVehiculo().getClienteId().toString(): "N/A";
        String tecnico = ot.getTecnico() != null ?
                ot.getTecnico().getIdUsuario().getNombre(): "Sin Asignar";

        return new OTResponseDTO(
                ot.getId(),
                ot.getCodigoOt(),
                vehiculo,
                cliente,
                tecnico,
                ot.getEstadoOt().getNombre(),
                ot.getCostoManoObra(),
                ot.getCostoRepuestos(),
                ot.getTotal(),
                ot.getFechaInicio(),
                ot.getFechaCierre()
        );
    }

    private FaseDTO toFaseDTO(FaseVehiculo fv){
        Long duracion = null;
        if (fv.getInicioAt() != null && fv.getFinAt() != null){
            duracion = ChronoUnit.MINUTES.between(fv.getInicioAt(), fv.getFinAt());
        }
        return new FaseDTO(
                fv.getId(),
                fv.getFase().getNombre(),
                fv.getFase().getOrden(),
                fv.getChecklistJson(),
                fv.getObservaciones(),
                fv.getInicioAt(),
                fv.getFinAt(),
                duracion
        );
    }
}
