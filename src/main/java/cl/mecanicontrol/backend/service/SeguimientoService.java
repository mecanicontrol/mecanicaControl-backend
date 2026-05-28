package cl.mecanicontrol.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.mecanicontrol.backend.dto.FaseSeguimientoDTO;
import cl.mecanicontrol.backend.dto.PrediccionSeguimientoDTO;
import cl.mecanicontrol.backend.dto.SeguimientoResponseDTO;
import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.entity.Fase;
import cl.mecanicontrol.backend.entity.FaseVehiculo;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.PrediccionTiempoOT;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.FaseRepository;
import cl.mecanicontrol.backend.repository.FaseVehiculoRepository;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.repository.PrediccionTiempoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final OrdenTrabajoRepository otRepo;
    private final FaseRepository faseRepo;
    private final FaseVehiculoRepository faseVehiculoRepo;
    private final PrediccionTiempoRepository prediccionRepo;
    private final ClienteRepository clienteRepo;
    private final MarcaVehiculoRepository marcaRepo;
    private final ModeloVehiculoRepository modeloRepo;
    private final ObjectMapper objectMapper;

    public SeguimientoResponseDTO buscarPorCodigo(String codigo) {
        OrdenTrabajo ot = otRepo.findByCodigoOt(codigo.toUpperCase())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Código no encontrado"));

        // Datos del vehículo y cliente desde el agendamiento
        String vehiculoPatente = null;
        String vehiculoDescripcion = null;
        String nombreCliente = null;
        String nombreServicio = null;

        Agendamiento ag = ot.getAgendamiento();
        if (ag != null) {
            if (ag.getServicio() != null) {
                nombreServicio = ag.getServicio().getNombreServicio();
            }
            Vehiculo v = ag.getIdVehiculo();
            if (v != null) {
                vehiculoPatente = v.getPatente();
                String marca  = v.getMarcaVehiculoId()  != null ? marcaRepo.findById(v.getMarcaVehiculoId()).map(m -> m.getNombre()).orElse(null)  : null;
                String modelo = v.getModeloVehiculoId() != null ? modeloRepo.findById(v.getModeloVehiculoId()).map(m -> m.getNombre()).orElse(null) : null;
                String anio   = v.getAnio() != null ? v.getAnio().toString() : null;
                vehiculoDescripcion = java.util.stream.Stream.of(marca, modelo, anio)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));

                if (v.getClienteId() != null) {
                    Cliente c = clienteRepo.findById(v.getClienteId()).orElse(null);
                    if (c != null && c.getUsuario() != null) {
                        nombreCliente = c.getUsuario().getNombre() + " " + c.getUsuario().getApellido();
                    }
                }
            }
        }

        // Nombre del técnico
        String nombreTecnico = null;
        if (ot.getTecnico() != null && ot.getTecnico().getIdUsuario() != null) {
            nombreTecnico = ot.getTecnico().getIdUsuario().getNombre()
                          + " " + ot.getTecnico().getIdUsuario().getApellido();
        }

        // Todas las fases del catálogo ordenadas
        List<Fase> todasFases = faseRepo.findAllByOrderByOrdenAsc();

        // Fases ya iniciadas/completadas para esta OT
        List<FaseVehiculo> fasesOT = faseVehiculoRepo.findByOrdenTrabajoId(ot.getId());
        Map<UUID, FaseVehiculo> fasesPorFaseId = fasesOT.stream()
            .collect(Collectors.toMap(fv -> fv.getFase().getId(), fv -> fv));

        // Predicción
        Optional<PrediccionTiempoOT> prediccionOpt = prediccionRepo.findByOrdenTrabajo_Id(ot.getId());
        Map<String, Integer> desglose = Map.of();
        PrediccionSeguimientoDTO prediccionDTO = null;

        if (prediccionOpt.isPresent()) {
            PrediccionTiempoOT p = prediccionOpt.get();
            try {
                if (p.getDesgloseFasesJson() != null && !p.getDesgloseFasesJson().isBlank()) {
                    desglose = objectMapper.readValue(p.getDesgloseFasesJson(), new TypeReference<>() {});
                }
            } catch (Exception e) {
                log.warn("No se pudo parsear desglose_fases_json para OT {}: {}", ot.getId(), e.getMessage());
            }
            prediccionDTO = new PrediccionSeguimientoDTO(
                p.getTiempoEstimadoMin(),
                p.getHoraFinEstimada(),
                p.getConfianzaPct(),
                desglose
            );
        }

        // Construir lista de fases con estado
        final Map<String, Integer> desgloseF = desglose;
        List<FaseSeguimientoDTO> fasesDTO = todasFases.stream().map(f -> {
            FaseVehiculo fv = fasesPorFaseId.get(f.getId());
            if (fv == null) {
                return new FaseSeguimientoDTO(f.getNombre(), f.getOrden().intValue(), "PENDIENTE", null, null, desgloseF.get(f.getNombre()));
            }
            String estado = fv.getFinAt() != null ? "COMPLETADA" : "ACTIVA";
            return new FaseSeguimientoDTO(f.getNombre(), f.getOrden().intValue(), estado, fv.getInicioAt(), fv.getFinAt(), desgloseF.get(f.getNombre()));
        }).toList();

        return new SeguimientoResponseDTO(
            ot.getId(),
            ot.getCodigoOt(),
            ot.getEstadoOt() != null ? ot.getEstadoOt().getNombre() : null,
            vehiculoPatente,
            vehiculoDescripcion,
            nombreCliente,
            nombreTecnico,
            nombreServicio,
            ot.getFechaInicio(),
            ot.getFechaCierre(),
            fasesDTO,
            prediccionDTO,
            ot.getCostoManoObra(),
            ot.getCostoRepuestos(),
            ot.getTotal(),
            ot.getDiagnostico(),
            ot.getTrabajoRealizado()
        );
    }
}