package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.ot.FaseDTO;
import cl.mecanicontrol.backend.dto.ot.SeguimientoPublicoDTO;
import cl.mecanicontrol.backend.entity.FaseVehiculo;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.repository.FaseVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seguimiento")
public class SeguimientoController {

    private final OrdenTrabajoRepository otRepository;
    private final FaseVehiculoRepository faseVehiculoRepository;

    public SeguimientoController(OrdenTrabajoRepository otRepository,
                                 FaseVehiculoRepository faseVehiculoRepository) {
        this.otRepository = otRepository;
        this.faseVehiculoRepository = faseVehiculoRepository;
    }

    @GetMapping("/{codigoOt}")
    public ResponseEntity<SeguimientoPublicoDTO> seguimiento(@PathVariable String codigoOt) {
        OrdenTrabajo ot = otRepository.findByCodigoOtEager(codigoOt)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT no encontrada"));
        return ResponseEntity.ok(toSeguimientoDTO(ot));
    }

    @GetMapping("/agendamiento/{agendamientoId}")
    public ResponseEntity<SeguimientoPublicoDTO> seguimientoPorAgendamiento(
            @PathVariable UUID agendamientoId) {
        OrdenTrabajo ot = otRepository.findByAgendamientoIdAgendamiento(agendamientoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay OT asociada a este agendamiento"));
        ot = otRepository.findByCodigoOtEager(ot.getCodigoOt())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT no encontrada"));
        return ResponseEntity.ok(toSeguimientoDTO(ot));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private SeguimientoPublicoDTO toSeguimientoDTO(OrdenTrabajo ot) {
        List<FaseDTO> fases = faseVehiculoRepository.findByOrdenTrabajoId(ot.getId())
                .stream().map(this::toFaseDTO).toList();

        FaseDTO faseActual = fases.stream()
                .filter(f -> f.finAt() == null)
                .findFirst().orElse(null);

        String vehiculo = ot.getAgendamiento() != null && ot.getAgendamiento().getIdVehiculo() != null
                ? ot.getAgendamiento().getIdVehiculo().getPatente() : "N/A";

        String nombreServicio = ot.getAgendamiento() != null && ot.getAgendamiento().getServicio() != null
                ? ot.getAgendamiento().getServicio().getNombreServicio() : null;

        String nombreTecnico = ot.getTecnico() != null && ot.getTecnico().getIdUsuario() != null
                ? ot.getTecnico().getIdUsuario().getNombre() + " " + ot.getTecnico().getIdUsuario().getApellido()
                : null;

        return new SeguimientoPublicoDTO(
                ot.getCodigoOt(),
                vehiculo,
                ot.getEstadoOt().getNombre(),
                fases,
                faseActual,
                null,
                nombreServicio,
                nombreTecnico,
                ot.getFechaInicio(),
                ot.getTotal(),
                ot.getCostoManoObra(),
                ot.getCostoRepuestos()
        );
    }

    private FaseDTO toFaseDTO(FaseVehiculo fv) {
        Long duracion = null;
        if (fv.getInicioAt() != null && fv.getFinAt() != null)
            duracion = ChronoUnit.MINUTES.between(fv.getInicioAt(), fv.getFinAt());

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