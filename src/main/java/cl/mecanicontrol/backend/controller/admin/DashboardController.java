package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.dto.admin.DashboardDTO;
import cl.mecanicontrol.backend.dto.admin.DashboardDTO.AgendamientoResumenDTO;
import cl.mecanicontrol.backend.dto.admin.DashboardDTO.ServicioConteoDTO;
import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.repository.AgendamientoRepository;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final AgendamientoRepository agendamientoRepo;
    private final ClienteRepository clienteRepo;
    private final TecnicoRepository tecnicoRepo;

    public DashboardController(AgendamientoRepository agendamientoRepo,
                               ClienteRepository clienteRepo,
                               TecnicoRepository tecnicoRepo) {
        this.agendamientoRepo = agendamientoRepo;
        this.clienteRepo      = clienteRepo;
        this.tecnicoRepo      = tecnicoRepo;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> dashboard() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia    = inicioDia.plusDays(1);

        List<Agendamiento> todos = agendamientoRepo.findAll();

        long agendamientosHoy = todos.stream()
                .filter(a -> a.getFechaInicio() != null
                        && !a.getFechaInicio().isBefore(inicioDia)
                        && a.getFechaInicio().isBefore(finDia))
                .count();

        long agendamientosPendientes = todos.stream()
                .filter(a -> a.getIdEstadoAgendamiento() != null
                        && "PENDIENTE".equalsIgnoreCase(a.getIdEstadoAgendamiento().getNombre()))
                .count();

        long tecnicosDisponibles = tecnicoRepo.findByDisponibleTrue().size();
        long totalClientes       = clienteRepo.count();

        Map<String, Long> porEstado = todos.stream()
                .filter(a -> a.getIdEstadoAgendamiento() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getIdEstadoAgendamiento().getNombre(),
                        Collectors.counting()
                ));

        Map<String, Long> serviciosConteo = todos.stream()
                .filter(a -> a.getServicio() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getServicio().getNombreServicio(),
                        Collectors.counting()
                ));
        List<ServicioConteoDTO> serviciosMas = serviciosConteo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new ServicioConteoDTO(e.getKey(), e.getValue()))
                .toList();

        List<AgendamientoResumenDTO> ultimos = todos.stream()
                .filter(a -> a.getFechaInicio() != null)
                .sorted(Comparator.comparing(Agendamiento::getFechaInicio).reversed())
                .limit(5)
                .map(this::toResumen)
                .toList();

        List<AgendamientoResumenDTO> proximosHoy = todos.stream()
                .filter(a -> a.getFechaInicio() != null
                        && !a.getFechaInicio().isBefore(LocalDateTime.now())
                        && a.getFechaInicio().isBefore(finDia))
                .sorted(Comparator.comparing(Agendamiento::getFechaInicio))
                .limit(10)
                .map(this::toResumen)
                .toList();

        return ResponseEntity.ok(new DashboardDTO(
                agendamientosHoy,
                agendamientosPendientes,
                tecnicosDisponibles,
                totalClientes,
                porEstado,
                serviciosMas,
                ultimos,
                proximosHoy
        ));
    }

    private AgendamientoResumenDTO toResumen(Agendamiento a) {
        String clienteNombre = "";
        if (a.getIdVehiculo() != null) {
            var cliente = clienteRepo.findById(a.getIdVehiculo().getClienteId()).orElse(null);
            if (cliente != null && cliente.getUsuario() != null) {
                clienteNombre = cliente.getUsuario().getNombre() + " " + cliente.getUsuario().getApellido();
            }
        }
        return new AgendamientoResumenDTO(
                a.getIdAgendamiento() != null ? a.getIdAgendamiento().toString() : null,
                clienteNombre,
                a.getServicio() != null ? a.getServicio().getNombreServicio() : "—",
                a.getIdVehiculo() != null ? a.getIdVehiculo().getPatente() : "—",
                a.getIdEstadoAgendamiento() != null ? a.getIdEstadoAgendamiento().getNombre() : "—",
                a.getFechaInicio() != null ? a.getFechaInicio().toString() : null
        );
    }
}
