package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import cl.mecanicontrol.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/propuestas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPropuestasController {

    private final PropuestaDiagnosticoRepository propuestaRepo;
    private final EmailService emailService;
    private final ClienteRepository clienteRepo;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public AdminPropuestasController(PropuestaDiagnosticoRepository propuestaRepo,
                                     EmailService emailService,
                                     ClienteRepository clienteRepo) {
        this.propuestaRepo = propuestaRepo;
        this.emailService  = emailService;
        this.clienteRepo   = clienteRepo;
    }

    /** Lista propuestas. Pasa ?estado=PENDIENTE_ADMIN (default) o ?estado=TODAS */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(
            @RequestParam(required = false, defaultValue = "PENDIENTE_ADMIN") String estado) {
        List<PropuestaDiagnostico> lista = "TODAS".equals(estado)
                ? propuestaRepo.findAll()
                : propuestaRepo.findByEstadoIn(Arrays.asList(estado.split(",")));
        return ResponseEntity.ok(lista.stream().map(this::toMap).toList());
    }

    @Transactional
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobar(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {

        PropuestaDiagnostico p = propuestaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!"PENDIENTE_ADMIN".equals(p.getEstado()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden aprobar propuestas PENDIENTE_ADMIN");

        String nota = body != null && body.containsKey("nota") ? body.get("nota").toString() : null;
        p.setNotaAdmin(nota);
        p.setEstado("ENVIADA_CLIENTE");
        propuestaRepo.save(p);
        enviarEmailPropuesta(p);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazar(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        PropuestaDiagnostico p = propuestaRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        p.setNotaAdmin(body.containsKey("nota") ? body.get("nota").toString() : "Sin nota");
        p.setEstado("RECHAZADA_ADMIN");
        p.setResueltoAt(LocalDateTime.now());
        propuestaRepo.save(p);
        return ResponseEntity.noContent().build();
    }

    // ── Email ────────────────────────────────────────────────────────────────

    private void enviarEmailPropuesta(PropuestaDiagnostico p) {
        try {
            OrdenTrabajo ot = p.getOrdenTrabajo();
            String emailCliente = "", nombreCliente = "Cliente";

            if (ot.getAgendamiento() != null && ot.getAgendamiento().getIdVehiculo() != null) {
                Vehiculo vehiculo = ot.getAgendamiento().getIdVehiculo();
                if (vehiculo.getClienteId() != null) {
                    Optional<Cliente> clienteOpt = clienteRepo.findById(vehiculo.getClienteId());
                    if (clienteOpt.isPresent() && clienteOpt.get().getUsuario() != null) {
                        emailCliente  = clienteOpt.get().getUsuario().getEmail();
                        nombreCliente = clienteOpt.get().getUsuario().getNombre();
                    }
                }
            }
            if (emailCliente.isBlank()) return;

            String link     = frontendUrl + "/confirmar-diagnostico/" + p.getTokenCliente();
            String codigoOt = ot.getCodigoOt();

            StringBuilder filas = new StringBuilder();
            for (PropuestaServicio ps : p.getServicios()) {
                filas.append("<tr>")
                     .append("<td style='padding:8px;border-bottom:1px solid #f0f0f0;font-weight:bold'>")
                     .append(ps.getServicio().getNombreServicio()).append("</td>")
                     .append("<td style='padding:8px;border-bottom:1px solid #f0f0f0;color:#555'>")
                     .append(ps.getDescripcion()).append("</td>")
                     .append("<td style='padding:8px;border-bottom:1px solid #f0f0f0;text-align:right;font-weight:bold'>$")
                     .append(ps.getServicio().getPrecioBase() != null ? ps.getServicio().getPrecioBase().toPlainString() : "0")
                     .append("</td></tr>");
            }

            BigDecimal total = p.getServicios().stream()
                    .map(ps -> ps.getServicio().getPrecioBase() != null ? ps.getServicio().getPrecioBase() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String notaHtml = p.getNotaTecnico() != null
                    ? "<p><strong>Nota del técnico:</strong> " + p.getNotaTecnico() + "</p>" : "";

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto'>"
                + "<div style='background:#1e293b;padding:24px;border-radius:12px 12px 0 0;text-align:center'>"
                + "<h2 style='color:#f97316;margin:0;font-size:22px'>Propuesta de diagnóstico</h2>"
                + "<p style='color:#94a3b8;margin:8px 0 0'>OT: <strong style='color:white'>" + codigoOt + "</strong></p>"
                + "</div>"
                + "<div style='background:white;padding:24px;border:1px solid #e2e8f0;border-top:none'>"
                + "<p>Hola <strong>" + nombreCliente + "</strong>,</p>"
                + "<p>El técnico ha revisado tu vehículo y propone los siguientes servicios:</p>"
                + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
                + "<thead><tr style='background:#f8fafc'>"
                + "<th style='padding:8px;text-align:left;font-size:12px;color:#64748b'>SERVICIO</th>"
                + "<th style='padding:8px;text-align:left;font-size:12px;color:#64748b'>JUSTIFICACIÓN</th>"
                + "<th style='padding:8px;text-align:right;font-size:12px;color:#64748b'>PRECIO</th>"
                + "</tr></thead><tbody>" + filas + "</tbody>"
                + "<tfoot><tr>"
                + "<td colspan='2' style='padding:12px 8px;font-weight:bold'>Total estimado</td>"
                + "<td style='padding:12px 8px;text-align:right;font-weight:bold;color:#f97316;font-size:18px'>$" + total.toPlainString() + "</td>"
                + "</tr></tfoot></table>"
                + notaHtml
                + "<div style='text-align:center;margin:24px 0'>"
                + "<a href='" + link + "' style='background:#f97316;color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:bold;font-size:16px'>"
                + "Ver y responder propuesta</a></div>"
                + "<p style='font-size:12px;color:#94a3b8;text-align:center'>Enlace personal e intransferible.</p>"
                + "</div></div>";

            emailService.enviar(emailCliente, "Propuesta de diagnóstico — " + codigoOt, html);
        } catch (Exception ignored) {}
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(PropuestaDiagnostico p) {
        OrdenTrabajo ot = p.getOrdenTrabajo();
        String patente = "", vehiculo = "", tecnico = "";
        if (ot != null && ot.getAgendamiento() != null && ot.getAgendamiento().getIdVehiculo() != null) {
            Vehiculo v = ot.getAgendamiento().getIdVehiculo();
            patente  = v.getPatente() != null ? v.getPatente() : "";
            vehiculo = v.getAlias()   != null ? v.getAlias()   : "";
        }
        if (ot != null && ot.getTecnico() != null && ot.getTecnico().getIdUsuario() != null) {
            Usuario u = ot.getTecnico().getIdUsuario();
            tecnico = (u.getNombre() + " " + (u.getApellido() != null ? u.getApellido() : "")).trim();
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",         p.getId().toString());
        m.put("estado",     p.getEstado());
        m.put("codigoOt",   ot != null ? ot.getCodigoOt() : "");
        m.put("otId",       ot != null ? ot.getId().toString() : "");
        m.put("patente",    patente);
        m.put("vehiculo",   vehiculo);
        m.put("tecnico",    tecnico);
        m.put("notaTecnico",p.getNotaTecnico());
        m.put("notaAdmin",  p.getNotaAdmin());
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
            return sm;
        }).toList());
        return m;
    }
}