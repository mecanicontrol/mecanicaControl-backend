package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.ot.FaseDTO;
import cl.mecanicontrol.backend.dto.ot.SeguimientoPublicoDTO;
import cl.mecanicontrol.backend.entity.FaseVehiculo;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.repository.FaseVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;
import java.util.List;

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

    public ResponseEntity<SeguimientoPublicoDTO> seguimiento (@PathVariable String codigoOt){
        OrdenTrabajo ot = otRepository.findByCodigoOt(codigoOt)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "OT no encontrada"));

        List<FaseDTO> fases = faseVehiculoRepository.findByOrdenTrabajoId(ot.getId())
                .stream().map(this::toFaseDTO).toList();

        FaseDTO faseActual = fases.stream()
                .filter(f -> f.finAt() == null)
                .findFirst().orElse(null);

        String vehiculo = ot.getAgendamiento() != null ?
                ot.getAgendamiento().getIdVehiculo().getPatente() : "N/A";

        SeguimientoPublicoDTO dto = new SeguimientoPublicoDTO(
                ot.getCodigoOt(),
                vehiculo,
                ot.getEstadoOt().getNombre(),
                fases,
                faseActual,
                null
        );

        return ResponseEntity.ok(dto);
    }

    private FaseDTO toFaseDTO(FaseVehiculo fv){
        Long duracion = null;

        if (fv.getInicioAt() != null && fv.getFinAt() != null) {
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
