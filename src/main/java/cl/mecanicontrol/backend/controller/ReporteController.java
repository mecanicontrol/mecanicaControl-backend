package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.reporte.*;
import cl.mecanicontrol.backend.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardKPIDTO> dashboard() {
        return ResponseEntity.ok(reporteService.getDashboardKPIs());
    }

    @GetMapping("/ingresos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IngresosPorPeriodoDTO>> ingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDateTime d = desde != null ? desde.atStartOfDay()
                : LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
        LocalDateTime h = hasta != null ? hasta.plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reporteService.getIngresosPorPeriodo(d, h));
    }

    @GetMapping("/servicios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServicioEstadisticaDTO>> servicios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDateTime d = desde != null ? desde.atStartOfDay()
                : LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
        LocalDateTime h = hasta != null ? hasta.plusDays(1).atStartOfDay()
                : LocalDate.now().plusDays(1).atStartOfDay();
        return ResponseEntity.ok(reporteService.getServiciosTop(d, h));
    }

    @GetMapping("/tecnicos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TecnicoProductividadDTO>> tecnicos() {
        return ResponseEntity.ok(reporteService.getTecnicosProductividad());
    }
}