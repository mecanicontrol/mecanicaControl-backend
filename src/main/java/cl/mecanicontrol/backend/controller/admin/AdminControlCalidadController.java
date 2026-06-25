package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.entity.FaseVehiculo;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.EstadoOtRepository;
import cl.mecanicontrol.backend.repository.FaseVehiculoRepository;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/control-calidad")
@PreAuthorize("hasRole('ADMIN')")
public class AdminControlCalidadController {

    private final FaseVehiculoRepository faseRepo;
    private final MarcaVehiculoRepository marcaRepo;
    private final ModeloVehiculoRepository modeloRepo;
    private final OrdenTrabajoRepository otRepo;
    private final EstadoOtRepository estadoOtRepo;

    public AdminControlCalidadController(FaseVehiculoRepository faseRepo,
                                          MarcaVehiculoRepository marcaRepo,
                                          ModeloVehiculoRepository modeloRepo,
                                          OrdenTrabajoRepository otRepo,
                                          EstadoOtRepository estadoOtRepo) {
        this.faseRepo     = faseRepo;
        this.marcaRepo    = marcaRepo;
        this.modeloRepo   = modeloRepo;
        this.otRepo       = otRepo;
        this.estadoOtRepo = estadoOtRepo;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Map<String, Object>>> getPendientes() {
        List<FaseVehiculo> pendientes = faseRepo.findPendingControlCalidad();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        List<Map<String, Object>> result = new ArrayList<>();
        for (FaseVehiculo fv : pendientes) {
            OrdenTrabajo ot = fv.getOrdenTrabajo();

            String patente  = "";
            String vehiculo = "";
            String servicio = "";
            String cliente  = "";
            String tecnico  = "";

            if (ot.getAgendamiento() != null) {
                Vehiculo v = ot.getAgendamiento().getIdVehiculo();
                if (v != null) {
                    patente = v.getPatente() != null ? v.getPatente() : "";
                    String marca  = v.getMarcaVehiculoId() != null
                            ? marcaRepo.findById(v.getMarcaVehiculoId()).map(m -> m.getNombre()).orElse("")
                            : "";
                    String modelo = v.getModeloVehiculoId() != null
                            ? modeloRepo.findById(v.getModeloVehiculoId()).map(m -> m.getNombre()).orElse("")
                            : "";
                    vehiculo = (marca + " " + modelo).trim();
                }
                if (ot.getAgendamiento().getServicio() != null)
                    servicio = ot.getAgendamiento().getServicio().getNombreServicio();
            }
            if (ot.getTecnico() != null && ot.getTecnico().getIdUsuario() != null) {
                tecnico = ot.getTecnico().getIdUsuario().getNombre()
                        + " " + ot.getTecnico().getIdUsuario().getApellido();
            }

            Map<String, Object> m = new java.util.HashMap<>();
            m.put("faseId",        fv.getId().toString());
            m.put("otId",          ot.getId().toString());
            m.put("codigoOt",      ot.getCodigoOt());
            m.put("patente",       patente);
            m.put("vehiculo",      vehiculo);
            m.put("servicio",      servicio);
            m.put("cliente",       cliente);
            m.put("tecnico",       tecnico.trim());
            m.put("observaciones", fv.getObservaciones() != null ? fv.getObservaciones() : "");
            m.put("imagenes",      fv.getImagenes() != null ? fv.getImagenes() : "[]");
            m.put("checklist",     fv.getChecklistJson() != null ? fv.getChecklistJson() : "[]");
            m.put("inicioAt",      fv.getInicioAt() != null ? fv.getInicioAt().format(fmt) : null);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @Transactional
    @PostMapping("/{faseId}/aprobar")
    public ResponseEntity<Void> aprobar(
            @PathVariable UUID faseId,
            @RequestBody(required = false) Map<String, Object> body) {

        FaseVehiculo fv = faseRepo.findById(faseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fase no encontrada"));

        if (!"CONTROL_CALIDAD".equals(fv.getFase().getNombre()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo aplica a CONTROL_CALIDAD");

        if (fv.getFinAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La fase ya estaba completada");

        String nota = (body != null && body.containsKey("nota")) ? body.get("nota").toString() : null;
        fv.setAprobacionEstado("APROBADA");
        fv.setAprobacionNota(nota);
        fv.setFinAt(LocalDateTime.now());
        faseRepo.save(fv);

        // Auto-avanzar estadoOt a LISTA_ENTREGA
        OrdenTrabajo ot = fv.getOrdenTrabajo();
        estadoOtRepo.findByNombre("LISTA_ENTREGA").ifPresent(ot::setEstadoOt);
        otRepo.save(ot);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/{faseId}/rechazar")
    public ResponseEntity<Void> rechazar(
            @PathVariable UUID faseId,
            @RequestBody Map<String, Object> body) {

        FaseVehiculo fv = faseRepo.findById(faseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fase no encontrada"));

        if (!"CONTROL_CALIDAD".equals(fv.getFase().getNombre()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo aplica a CONTROL_CALIDAD");

        if (fv.getFinAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La fase ya estaba completada");

        String nota = body.containsKey("nota") ? body.get("nota").toString() : "Rechazado sin nota";
        fv.setAprobacionEstado("RECHAZADA");
        fv.setAprobacionNota(nota);
        faseRepo.save(fv);

        return ResponseEntity.noContent().build();
    }
}