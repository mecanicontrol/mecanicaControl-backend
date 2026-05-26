package cl.mecanicontrol.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.mecanicontrol.backend.dto.PrediccionResponseDTO;
import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.PrediccionTiempoOT;
import cl.mecanicontrol.backend.entity.ServicioCatalogo;
import cl.mecanicontrol.backend.entity.Tecnico;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.EspecialidadRepository;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.repository.PrediccionTiempoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrediccionTiempoService {

    private static final String MODELO_IA = "llama-3.3-70b-versatile";
    private static final List<String> FASES = List.of(
        "RECEPCION", "DIAGNOSTICO", "EN_TRABAJO", "CONTROL_CALIDAD", "LISTO_ENTREGA"
    );

    private final OrdenTrabajoRepository otRepo;
    private final PrediccionTiempoRepository prediccionRepo;
    private final EspecialidadRepository especialidadRepo;
    private final MarcaVehiculoRepository marcaRepo;
    private final ModeloVehiculoRepository modeloRepo;
    private final GroqService groqService;
    private final ObjectMapper objectMapper;

    // ── POST /api/ia/predecir-tiempo?otId= ──────────────────────────────────

    public PrediccionResponseDTO predecir(UUID otId) {
        OrdenTrabajo ot = otRepo.findById(otId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OT no encontrada"));

        // Contexto del vehículo y servicio
        String marcaNombre     = "desconocida";
        String modeloNombre    = "desconocido";
        String anio            = "desconocido";
        String kilometraje     = "sin datos";
        String servicioNombre  = "desconocido";
        String categoriaNombre = "desconocida";
        String categoriaId     = null;
        Integer duracionCat    = null;
        String notas           = null;

        Agendamiento ag = ot.getAgendamiento();
        if (ag != null) {
            ServicioCatalogo sc = ag.getServicio();
            if (sc != null) {
                servicioNombre = sc.getNombreServicio();
                duracionCat    = sc.getDuracion();
                if (sc.getCategoriaServicio() != null) {
                    categoriaNombre = sc.getCategoriaServicio().getNombreCategoriaServicio();
                    categoriaId     = sc.getCategoriaServicio().getId_categoria_servicio().toString();
                }
            }
            notas = ag.getNotaCliente();

            Vehiculo v = ag.getIdVehiculo();
            if (v != null) {
                if (v.getMarcaVehiculoId() != null)
                    marcaNombre = marcaRepo.findById(v.getMarcaVehiculoId())
                        .map(m -> m.getNombre()).orElse("desconocida");
                if (v.getModeloVehiculoId() != null)
                    modeloNombre = modeloRepo.findById(v.getModeloVehiculoId())
                        .map(m -> m.getNombre()).orElse("desconocido");
                if (v.getAnio() != null)       anio       = v.getAnio().toString();
                if (v.getKilometrajeIngreso() != null)
                    kilometraje = v.getKilometrajeIngreso() + " km";
            }
        }

        // Contexto del técnico
        String nivelTecnico   = "desconocido";
        String especialidades = "ninguna registrada";

        Tecnico tec = ot.getTecnico();
        if (tec != null) {
            if (tec.getIdNivelTecnico() != null)
                nivelTecnico = tec.getIdNivelTecnico().getNombre();
            if (tec.getIdTecnico() != null) {
                List<String> esList = especialidadRepo.findNombresByTecnicoId(tec.getIdTecnico());
                if (!esList.isEmpty()) especialidades = String.join(", ", esList);
            }
        }

        // Historial promedio de la misma categoría
        Map<String, Double> historico = cargarHistorico(categoriaId);

        // Llamar al LLM
        String prompt = buildPrompt(
            marcaNombre, modeloNombre, anio, kilometraje,
            servicioNombre, categoriaNombre, duracionCat,
            nivelTecnico, especialidades, historico, notas
        );

        Map<String, Integer> desglose  = new LinkedHashMap<>();
        int totalMin                   = duracionCat != null ? duracionCat : 90;
        BigDecimal confianza           = BigDecimal.valueOf(60);

        try {
            String raw  = groqService.llamar(prompt);
            log.info("Groq response OT {}: {}", ot.getCodigoOt(), raw);
            String json = extractJson(raw);
            JsonNode root = objectMapper.readTree(json);

            totalMin = root.path("tiempoEstimadoMin").asInt(totalMin);
            confianza = BigDecimal.valueOf(root.path("confianzaPct").asDouble(60.0));

            JsonNode desgloseNode = root.path("desglosePorFase");
            if (desgloseNode.isObject())
                desgloseNode.fields().forEachRemaining(e -> desglose.put(e.getKey(), e.getValue().asInt(0)));

        } catch (Exception e) {
            log.warn("Groq parse error OT {}: {} — usando fallback", ot.getCodigoOt(), e.getMessage());
            desglose.putAll(desgloseDefault(totalMin, historico));
        }

        for (String f : FASES) desglose.putIfAbsent(f, 0);

        LocalDateTime inicio = ot.getFechaInicio() != null ? ot.getFechaInicio() : LocalDateTime.now();
        LocalDateTime fin    = inicio.plusMinutes(totalMin);

        // Upsert predicción
        PrediccionTiempoOT ent = prediccionRepo.findByOrdenTrabajo_Id(otId)
            .orElseGet(() -> { PrediccionTiempoOT n = new PrediccionTiempoOT(); n.setOrdenTrabajo(ot); return n; });

        ent.setTiempoEstimadoMin(totalMin);
        ent.setHoraInicioEstimada(inicio);
        ent.setHoraFinEstimada(fin);
        ent.setConfianzaPct(confianza);
        ent.setModeloIa(MODELO_IA);
        try { ent.setDesgloseFasesJson(objectMapper.writeValueAsString(desglose)); } catch (Exception ignored) {}
        prediccionRepo.save(ent);

        return new PrediccionResponseDTO(ot.getCodigoOt(), totalMin, inicio, fin, confianza, desglose, MODELO_IA);
    }

    // ── GET /api/ia/prediccion/{otId} ────────────────────────────────────────

    public PrediccionResponseDTO obtenerPrediccion(UUID otId) {
        PrediccionTiempoOT p = prediccionRepo.findByOrdenTrabajo_Id(otId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Predicción no encontrada"));

        Map<String, Integer> desglose = new LinkedHashMap<>();
        try {
            if (p.getDesgloseFasesJson() != null)
                desglose = objectMapper.readValue(p.getDesgloseFasesJson(), new TypeReference<>() {});
        } catch (Exception ignored) {}

        OrdenTrabajo ot = p.getOrdenTrabajo();
        return new PrediccionResponseDTO(
            ot.getCodigoOt(), p.getTiempoEstimadoMin(),
            p.getHoraInicioEstimada(), p.getHoraFinEstimada(),
            p.getConfianzaPct(), desglose, p.getModeloIa()
        );
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    private Map<String, Double> cargarHistorico(String categoriaId) {
        Map<String, Double> m = new LinkedHashMap<>();
        try {
            for (Object[] row : prediccionRepo.avgDuracionPorFase(categoriaId)) {
                String fase = (String) row[0];
                Double avg  = row[1] instanceof Number n ? n.doubleValue() : 0.0;
                m.put(fase, avg);
            }
        } catch (Exception e) {
            log.warn("Historial no disponible: {}", e.getMessage());
        }
        return m;
    }

    private String buildPrompt(
            String marca, String modelo, String anio, String km,
            String servicio, String categoria, Integer duracionCat,
            String nivelTecnico, String especialidades,
            Map<String, Double> historico, String notas) {

        var sb = new StringBuilder();
        sb.append("Eres un sistema experto en talleres mecánicos. ")
          .append("Responde SOLO con un objeto JSON válido, sin texto adicional.\n\n")
          .append("=== VEHÍCULO ===\n")
          .append("Marca: ").append(marca).append("\n")
          .append("Modelo: ").append(modelo).append("\n")
          .append("Año: ").append(anio).append("\n")
          .append("Kilometraje: ").append(km).append("\n\n")
          .append("=== SERVICIO ===\n")
          .append("Tipo: ").append(servicio).append("\n")
          .append("Categoría: ").append(categoria).append("\n")
          .append("Duración en catálogo: ")
          .append(duracionCat != null ? duracionCat + " min" : "no especificado").append("\n\n")
          .append("=== TÉCNICO ASIGNADO ===\n")
          .append("Nivel: ").append(nivelTecnico).append("\n")
          .append("Especialidades: ").append(especialidades).append("\n");

        if (!historico.isEmpty()) {
            sb.append("\n=== HISTORIAL PROMEDIO (misma categoría, minutos) ===\n");
            historico.forEach((f, avg) ->
                sb.append("  ").append(f).append(": ").append(String.format("%.1f", avg)).append(" min\n"));
        }

        if (notas != null && !notas.isBlank())
            sb.append("\n=== NOTAS DEL CLIENTE ===\n").append(notas).append("\n");

        sb.append("""

            === INSTRUCCIONES ===
            Factores a considerar:
            - Kilometraje alto implica mayor desgaste y más tiempo en DIAGNOSTICO y EN_TRABAJO.
            - Nivel SENIOR reduce tiempos vs JUNIOR; especialidades relevantes mejoran eficiencia.
            - El historial real (si disponible) es el factor más confiable.
            - Vehículos con más antigüedad pueden requerir más tiempo.

            Responde ÚNICAMENTE con este JSON (sin bloques de código, sin explicaciones):
            {
              "tiempoEstimadoMin": <entero, suma de las fases>,
              "desglosePorFase": {
                "RECEPCION": <minutos>,
                "DIAGNOSTICO": <minutos>,
                "EN_TRABAJO": <minutos>,
                "CONTROL_CALIDAD": <minutos>,
                "LISTO_ENTREGA": <minutos>
              },
              "confianzaPct": <decimal 0-100>
            }
            """);

        return sb.toString();
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        return (start >= 0 && end > start) ? text.substring(start, end + 1) : text;
    }

    private Map<String, Integer> desgloseDefault(int total, Map<String, Double> hist) {
        Map<String, Integer> d = new LinkedHashMap<>();
        if (!hist.isEmpty()) {
            double sum = hist.values().stream().mapToDouble(v -> v).sum();
            for (String f : FASES) {
                double ratio = sum > 0 ? hist.getOrDefault(f, 0.0) / sum : 0.2;
                d.put(f, (int) Math.round(total * ratio));
            }
        } else {
            d.put("RECEPCION",       (int)(total * 0.07));
            d.put("DIAGNOSTICO",     (int)(total * 0.20));
            d.put("EN_TRABAJO",      (int)(total * 0.55));
            d.put("CONTROL_CALIDAD", (int)(total * 0.10));
            d.put("LISTO_ENTREGA",   (int)(total * 0.08));
        }
        return d;
    }
}