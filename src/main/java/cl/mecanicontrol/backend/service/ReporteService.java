package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.reporte.*;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.Tecnico;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final OrdenTrabajoRepository otRepo;
    private final ClienteRepository clienteRepo;
    private final TecnicoRepository tecnicoRepo;

    private static final List<String> ESTADOS_ACTIVOS =
            List.of("RECIBIDA", "EN_DIAGNOSTICO", "EN_TRABAJO", "EN_REVISION");
    private static final String ESTADO_COMPLETADA = "COMPLETADA";

    public ReporteService(OrdenTrabajoRepository otRepo,
                          ClienteRepository clienteRepo,
                          TecnicoRepository tecnicoRepo) {
        this.otRepo = otRepo;
        this.clienteRepo = clienteRepo;
        this.tecnicoRepo = tecnicoRepo;
    }

    // ── Dashboard KPIs ────────────────────────────────────────────────────────

    public DashboardKPIDTO getDashboardKPIs() {
        List<OrdenTrabajo> todas = otRepo.findAllEager();

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes    = inicioMes.plusMonths(1);

        int otActivas = (int) todas.stream()
                .filter(ot -> ESTADOS_ACTIVOS.contains(ot.getEstadoOt().getNombre()))
                .count();

        List<OrdenTrabajo> completadasMes = todas.stream()
                .filter(ot -> ESTADO_COMPLETADA.equals(ot.getEstadoOt().getNombre())
                        && ot.getFechaCierre() != null
                        && !ot.getFechaCierre().isBefore(inicioMes)
                        && ot.getFechaCierre().isBefore(finMes))
                .toList();

        BigDecimal ingresosMes = completadasMes.stream()
                .map(ot -> ot.getTotal() != null ? ot.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosTotal = todas.stream()
                .filter(ot -> ESTADO_COMPLETADA.equals(ot.getEstadoOt().getNombre()))
                .map(ot -> ot.getTotal() != null ? ot.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long tecnicosDisponibles = tecnicoRepo.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getDisponible()))
                .count();

        long clientesTotales = clienteRepo.count();

        LocalDateTime inicioMesUsuario = inicioMes;
        long clientesNuevosMes = clienteRepo.findAll().stream()
                .filter(c -> c.getUsuario() != null
                        && c.getUsuario().getCreatedAt() != null
                        && !c.getUsuario().getCreatedAt().isBefore(inicioMesUsuario)
                        && c.getUsuario().getCreatedAt().isBefore(finMes))
                .count();

        // Servicio top (por agendamientos en OTs)
        Map<String, Long> conteoServicios = todas.stream()
                .filter(ot -> ot.getAgendamiento() != null && ot.getAgendamiento().getServicio() != null)
                .collect(Collectors.groupingBy(
                        ot -> ot.getAgendamiento().getServicio().getNombreServicio(),
                        Collectors.counting()
                ));
        String servicioTopNombre = conteoServicios.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("—");
        int servicioTopCount = (int) conteoServicios.getOrDefault(servicioTopNombre, 0L).longValue();

        return new DashboardKPIDTO(
                otActivas,
                completadasMes.size(),
                ingresosMes,
                ingresosTotal,
                (int) tecnicosDisponibles,
                (int) clientesTotales,
                (int) clientesNuevosMes,
                servicioTopNombre,
                servicioTopCount
        );
    }

    // ── Ingresos por periodo ───────────────────────────────────────────────────

    public List<IngresosPorPeriodoDTO> getIngresosPorPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        List<OrdenTrabajo> ots = otRepo.findByFechaInicioBetween(desde, hasta);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<OrdenTrabajo>> porMes = ots.stream()
                .filter(ot -> ESTADO_COMPLETADA.equals(ot.getEstadoOt().getNombre())
                        && ot.getFechaCierre() != null)
                .collect(Collectors.groupingBy(ot -> ot.getFechaCierre().format(fmt)));

        // Genera todos los meses en el rango aunque no tengan datos
        List<String> meses = new ArrayList<>();
        LocalDateTime cursor = desde.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        while (!cursor.isAfter(hasta)) {
            meses.add(cursor.format(fmt));
            cursor = cursor.plusMonths(1);
        }

        return meses.stream().map(mes -> {
            List<OrdenTrabajo> lista = porMes.getOrDefault(mes, List.of());
            BigDecimal total = lista.stream()
                    .map(ot -> ot.getTotal() != null ? ot.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new IngresosPorPeriodoDTO(mes, total, lista.size());
        }).toList();
    }

    // ── Servicios estadísticas ─────────────────────────────────────────────────

    public List<ServicioEstadisticaDTO> getServiciosTop(LocalDateTime desde, LocalDateTime hasta) {
        List<OrdenTrabajo> ots = otRepo.findByFechaInicioBetween(desde, hasta);

        record Acum(int count, BigDecimal ingreso) {}

        Map<String, Acum> map = new LinkedHashMap<>();
        for (OrdenTrabajo ot : ots) {
            if (ot.getAgendamiento() == null || ot.getAgendamiento().getServicio() == null) continue;
            String nombre = ot.getAgendamiento().getServicio().getNombreServicio();
            BigDecimal ing = (ESTADO_COMPLETADA.equals(ot.getEstadoOt().getNombre()) && ot.getTotal() != null)
                    ? ot.getTotal() : BigDecimal.ZERO;
            Acum prev = map.getOrDefault(nombre, new Acum(0, BigDecimal.ZERO));
            map.put(nombre, new Acum(prev.count() + 1, prev.ingreso().add(ing)));
        }

        return map.entrySet().stream()
                .sorted((a, b) -> b.getValue().count() - a.getValue().count())
                .map(e -> new ServicioEstadisticaDTO(e.getKey(), e.getValue().count(), e.getValue().ingreso()))
                .toList();
    }

    // ── Productividad técnicos ────────────────────────────────────────────────

    public List<TecnicoProductividadDTO> getTecnicosProductividad() {
        List<OrdenTrabajo> todas = otRepo.findAllEager();
        List<Tecnico> tecnicos = tecnicoRepo.findAll();

        record Acum(int completadas, int activas, BigDecimal ingreso) {}

        Map<UUID, Acum> map = new LinkedHashMap<>();
        for (OrdenTrabajo ot : todas) {
            if (ot.getTecnico() == null) continue;
            UUID tid = ot.getTecnico().getIdTecnico();
            boolean completada = ESTADO_COMPLETADA.equals(ot.getEstadoOt().getNombre());
            boolean activa = ESTADOS_ACTIVOS.contains(ot.getEstadoOt().getNombre());
            BigDecimal ing = (completada && ot.getTotal() != null) ? ot.getTotal() : BigDecimal.ZERO;
            Acum prev = map.getOrDefault(tid, new Acum(0, 0, BigDecimal.ZERO));
            map.put(tid, new Acum(
                    prev.completadas() + (completada ? 1 : 0),
                    prev.activas() + (activa ? 1 : 0),
                    prev.ingreso().add(ing)
            ));
        }

        return tecnicos.stream()
                .filter(t -> t.getIdUsuario() != null)
                .map(t -> {
                    Acum a = map.getOrDefault(t.getIdTecnico(), new Acum(0, 0, BigDecimal.ZERO));
                    String nombre = t.getIdUsuario().getNombre() + " " + t.getIdUsuario().getApellido();
                    return new TecnicoProductividadDTO(nombre, a.completadas(), a.activas(), a.ingreso());
                })
                .sorted(Comparator.comparingInt(TecnicoProductividadDTO::otCompletadas).reversed())
                .toList();
    }
}