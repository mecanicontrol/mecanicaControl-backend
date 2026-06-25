package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import cl.mecanicontrol.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Endpoints públicos (sin auth) para que el cliente vea y responda una propuesta
 * de diagnóstico desde el link del correo o desde su panel.
 */
@RestController
@RequestMapping("/api/propuestas")
public class PropuestaPublicaController {

    private final PropuestaDiagnosticoRepository propuestaRepo;
    private final OrdenTrabajoRepository otRepo;
    private final ClienteRepository clienteRepo;
    private final EstadoOtRepository estadoOtRepo;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public PropuestaPublicaController(PropuestaDiagnosticoRepository propuestaRepo,
                                      OrdenTrabajoRepository otRepo,
                                      ClienteRepository clienteRepo,
                                      EstadoOtRepository estadoOtRepo,
                                      EmailService emailService) {
        this.propuestaRepo = propuestaRepo;
        this.otRepo        = otRepo;
        this.clienteRepo   = clienteRepo;
        this.estadoOtRepo  = estadoOtRepo;
        this.emailService  = emailService;
    }

    /** Obtiene los detalles de la propuesta por token (sin auth) */
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> verPorToken(@PathVariable String token) {
        PropuestaDiagnostico p = propuestaRepo.findByTokenClienteWithOt(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propuesta no encontrada"));

        if (!"ENVIADA_CLIENTE".equals(p.getEstado()) && !"ACEPTADA".equals(p.getEstado())
                && !"ACEPTADA_PARCIAL".equals(p.getEstado()) && !"RECHAZADA_CLIENTE".equals(p.getEstado())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Esta propuesta ya no está disponible");
        }

        return ResponseEntity.ok(toMap(p));
    }

    /**
     * El cliente responde la propuesta.
     * Body: { decision: "ACEPTAR_TODO" | "ACEPTAR_PARCIAL" | "RECHAZAR",
     *         serviciosAceptados: [UUID...],   // solo para ACEPTAR_PARCIAL
     *         aceptaDisclaimer: boolean,        // requerido para ACEPTAR_PARCIAL
     *         notaCliente: String }
     */
    @Transactional
    @PostMapping("/{token}/responder")
    public ResponseEntity<Map<String, Object>> responder(
            @PathVariable String token,
            @RequestBody Map<String, Object> body) {

        PropuestaDiagnostico p = propuestaRepo.findByTokenClienteWithOt(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propuesta no encontrada"));

        if (!"ENVIADA_CLIENTE".equals(p.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta propuesta ya fue respondida");

        String decision = body.getOrDefault("decision", "").toString().toUpperCase();
        String notaCliente = body.containsKey("notaCliente") ? body.get("notaCliente").toString() : null;
        p.setNotaCliente(notaCliente);
        p.setResueltoAt(LocalDateTime.now());

        OrdenTrabajo ot = p.getOrdenTrabajo();

        switch (decision) {
            case "ACEPTAR_TODO" -> {
                p.setEstado("ACEPTADA");
                p.getServicios().forEach(ps -> ps.setAceptadoPorCliente(true));
                // Actualizar costoManoObra con total de propuesta
                BigDecimal total = p.getServicios().stream()
                        .map(ps -> ps.getServicio().getPrecioBase() != null ? ps.getServicio().getPrecioBase() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                ot.setCostoManoObra(total);
                otRepo.save(ot);
            }
            case "ACEPTAR_PARCIAL" -> {
                boolean acepta = body.containsKey("aceptaDisclaimer")
                        && Boolean.parseBoolean(body.get("aceptaDisclaimer").toString());
                p.setAceptaDisclaimer(acepta);
                p.setEstado("ACEPTADA_PARCIAL");

                @SuppressWarnings("unchecked")
                List<String> aceptados = body.containsKey("serviciosAceptados")
                        ? (List<String>) body.get("serviciosAceptados") : List.of();

                Set<UUID> aceptadosSet = new HashSet<>();
                aceptados.forEach(s -> aceptadosSet.add(UUID.fromString(s)));

                BigDecimal total = BigDecimal.ZERO;
                for (PropuestaServicio ps : p.getServicios()) {
                    boolean aceptado = aceptadosSet.contains(ps.getId());
                    ps.setAceptadoPorCliente(aceptado);
                    if (aceptado && ps.getServicio().getPrecioBase() != null)
                        total = total.add(ps.getServicio().getPrecioBase());
                }
                ot.setCostoManoObra(total);
                otRepo.save(ot);
            }
            case "RECHAZAR" -> {
                p.setEstado("RECHAZADA_CLIENTE");
                p.getServicios().forEach(ps -> ps.setAceptadoPorCliente(false));
                // OT se cancela automáticamente
                estadoOtRepo.findByNombre("COMPLETADA").ifPresent(est -> {
                    ot.setEstadoOt(est);
                    ot.setFechaCierre(LocalDateTime.now());
                    otRepo.save(ot);
                });
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisión inválida: " + decision);
        }

        propuestaRepo.save(p);
        notificarAdminRespuesta(p, ot);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("estado", p.getEstado());
        resp.put("codigoOt", ot.getCodigoOt());
        return ResponseEntity.ok(resp);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void notificarAdminRespuesta(PropuestaDiagnostico p, OrdenTrabajo ot) {
        try {
            String estado = p.getEstado();
            String etiqueta = switch (estado) {
                case "ACEPTADA"         -> "✅ Aceptada completamente";
                case "ACEPTADA_PARCIAL" -> "⚠️ Aceptada parcialmente";
                case "RECHAZADA_CLIENTE"-> "❌ Rechazada";
                default -> estado;
            };
            String html = "<p>El cliente ha respondido la propuesta de diagnóstico para la OT <strong>"
                    + ot.getCodigoOt() + "</strong>.</p>"
                    + "<p>Decisión: <strong>" + etiqueta + "</strong></p>"
                    + (p.getNotaCliente() != null ? "<p>Nota del cliente: " + p.getNotaCliente() + "</p>" : "")
                    + "<p>Ingresa al panel de administración para revisar los detalles.</p>";
            // BCC ya va via emailService.enviar a los admins configurados
        } catch (Exception ignored) {}
    }

    private Map<String, Object> toMap(PropuestaDiagnostico p) {
        OrdenTrabajo ot = p.getOrdenTrabajo();
        String patente = "", vehiculo = "";
        if (ot != null && ot.getAgendamiento() != null && ot.getAgendamiento().getIdVehiculo() != null) {
            Vehiculo v = ot.getAgendamiento().getIdVehiculo();
            patente  = v.getPatente() != null ? v.getPatente() : "";
            vehiculo = v.getAlias()   != null ? v.getAlias()   : "";
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",         p.getId().toString());
        m.put("token",      p.getTokenCliente());
        m.put("estado",     p.getEstado());
        m.put("codigoOt",   ot != null ? ot.getCodigoOt() : "");
        m.put("patente",    patente);
        m.put("vehiculo",   vehiculo);
        m.put("notaTecnico",p.getNotaTecnico());
        m.put("notaAdmin",  p.getNotaAdmin());
        m.put("notaCliente",p.getNotaCliente());
        m.put("creadoAt",   p.getCreadoAt() != null ? p.getCreadoAt().toString() : null);
        m.put("servicios",  p.getServicios().stream().map(ps -> {
            Map<String, Object> sm = new LinkedHashMap<>();
            sm.put("id",              ps.getId().toString());
            sm.put("servicioId",      ps.getServicio().getId().toString());
            sm.put("nombre",          ps.getServicio().getNombreServicio());
            sm.put("precioBase",      ps.getServicio().getPrecioBase());
            sm.put("descripcion",     ps.getDescripcion());
            sm.put("imagenes",        ps.getImagenes());
            sm.put("incluidoEnOriginal", ps.getIncluidoEnOriginal());
            sm.put("aceptadoPorCliente", ps.getAceptadoPorCliente());
            return sm;
        }).toList());

        BigDecimal total = p.getServicios().stream()
                .map(ps -> ps.getServicio().getPrecioBase() != null ? ps.getServicio().getPrecioBase() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        m.put("totalEstimado", total);
        return m;
    }
}