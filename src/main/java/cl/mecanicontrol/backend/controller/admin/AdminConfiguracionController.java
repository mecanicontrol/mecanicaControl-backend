package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.service.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/configuracion")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracionController {

    private final ConfiguracionService configuracionService;

    public AdminConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/bcc-admins")
    public ResponseEntity<Map<String, Object>> getBccAdmins() {
        List<String> correos = configuracionService.getBccAdminsList();
        return ResponseEntity.ok(Map.of("correos", correos));
    }

    @PutMapping("/bcc-admins")
    public ResponseEntity<Map<String, Object>> setBccAdmins(
            @RequestBody Map<String, List<String>> body) {
        List<String> correos = body.get("correos");
        if (correos == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campo 'correos' requerido"));
        }
        configuracionService.setBccAdmins(correos);
        return ResponseEntity.ok(Map.of("ok", true, "correos", correos));
    }

    @GetMapping("/capacidad-taller")
    public ResponseEntity<Map<String, Object>> getCapacidadTaller() {
        return ResponseEntity.ok(Map.of("capacidad", configuracionService.getCapacidadMaximaTaller()));
    }

    @PutMapping("/capacidad-taller")
    public ResponseEntity<Map<String, Object>> setCapacidadTaller(
            @RequestBody Map<String, Integer> body) {
        Integer cap = body.get("capacidad");
        if (cap == null || cap < 1) {
            return ResponseEntity.badRequest().body(Map.of("error", "Capacidad inválida"));
        }
        configuracionService.setCapacidadMaximaTaller(cap);
        return ResponseEntity.ok(Map.of("ok", true, "capacidad", cap));
    }
}