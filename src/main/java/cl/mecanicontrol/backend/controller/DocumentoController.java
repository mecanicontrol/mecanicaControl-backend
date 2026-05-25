package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.service.InformeOTService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class DocumentoController {

    private final InformeOTService informeOTService;

    public DocumentoController(InformeOTService informeOTService) {
        this.informeOTService = informeOTService;
    }

    @GetMapping("/api/ordenes/{id}/pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public ResponseEntity<Map<String, String>> generarPDF(@PathVariable UUID id){
        String url = informeOTService.generarPDF(id);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/api/documentos/{id}")
    public ResponseEntity<Map<String, String>> getDocumento(@PathVariable UUID id) {
        // Retorna URL firmada del Storage — pendiente integración Supabase
        String url = "https://supabase.co/storage/v1/object/public/documentos/" + id;
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/api/documentos/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECNICO')")
    public ResponseEntity<Map<String, String>> upload() {
        // Pendiente integración Supabase Storage SDK
        return ResponseEntity.ok(Map.of("mensaje", "Upload pendiente integración Supabase"));
    }
}
