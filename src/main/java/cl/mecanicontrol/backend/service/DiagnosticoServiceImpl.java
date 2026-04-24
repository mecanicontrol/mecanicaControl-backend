package cl.mecanicontrol.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.mecanicontrol.backend.dto.DiagnosticoRequestDTO;
import cl.mecanicontrol.backend.dto.DiagnosticoResponseDTO;
import cl.mecanicontrol.backend.entity.Diagnostico;
import cl.mecanicontrol.backend.repository.DiagnosticoRepository;
import cl.mecanicontrol.backend.service.GroqService;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosticoServiceImpl implements DiagnosticoService {
    private final GroqService groqService;
    private final ServicioCatalogoRepository servicioRepo;
    private final DiagnosticoRepository diagnosticoRepo;
    private final ObjectMapper objectMapper;

    @Override
    public DiagnosticoResponseDTO diagnosticar(DiagnosticoRequestDTO request) {

        // 1. Obtener catálogo real de servicios activos desde la BD
        String catalogo = servicioRepo.findByServicioActivoTrue()
            .stream()
            .map(s -> "- " + s.getNombreServicio() +
                      " (Precio: $" + s.getPrecioBase() +
                      " CLP, Duración: " + s.getDuracion() + " min)")
            .collect(Collectors.joining("\n"));

        // 2. Construir el prompt
        String prompt = construirPrompt(request, catalogo);

        // 3. Llamar a Groq
        String respuestaRaw = groqService.llamar(prompt);

        // 4. Parsear la respuesta JSON
        DiagnosticoResponseDTO response = parsearRespuesta(respuestaRaw);

        // 5. Guardar en BD
        guardarDiagnostico(request, respuestaRaw, response);

        return response;
    }

    private String construirPrompt(DiagnosticoRequestDTO req, String catalogo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un mecánico experto chileno del taller MecánicaHub. ");
        sb.append("Un cliente describe los fallos de su vehículo. ");
        sb.append("Recomienda SOLO servicios del catálogo disponible.\n\n");

        sb.append("DATOS DEL VEHÍCULO:\n");
        if (req.marca()       != null) sb.append("- Marca: ").append(req.marca()).append("\n");
        if (req.modelo()      != null) sb.append("- Modelo: ").append(req.modelo()).append("\n");
        if (req.anio()        != null) sb.append("- Año: ").append(req.anio()).append("\n");
        if (req.kilometraje() != null) sb.append("- Kilometraje: ").append(req.kilometraje()).append(" km\n");

        sb.append("\nDESCRIPCIÓN DEL PROBLEMA:\n").append(req.descripcionFallo()).append("\n\n");
        sb.append("CATÁLOGO DE SERVICIOS DISPONIBLES:\n").append(catalogo).append("\n\n");

        sb.append("""
            Responde ÚNICAMENTE en JSON con esta estructura exacta, sin texto adicional:
            {
              "resumenDiagnostico": "explicación breve del problema en 1-3 oraciones",
              "nivelUrgencia": "URGENTE|NORMAL|PREVENTIVO",
              "recomendacionGeneral": "consejo general al cliente",
              "serviciosRecomendados": [
                {
                  "nombre": "nombre exacto del servicio del catálogo",
                  "descripcion": "por qué se recomienda",
                  "probabilidad": "ALTA|MEDIA|BAJA",
                  "precioBase": 00000,
                  "urgencia": "URGENTE|NORMAL|PREVENTIVO"
                }
              ]
            }
            Máximo 4 servicios recomendados. Solo del catálogo.
            """);

        return sb.toString();
    }

    private DiagnosticoResponseDTO parsearRespuesta(String raw) {
        try {
            String json = raw
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
            return objectMapper.readValue(json, DiagnosticoResponseDTO.class);
        } catch (Exception e) {
            log.error("Error parseando respuesta Groq: {}", e.getMessage());
            return new DiagnosticoResponseDTO(
                "No se pudo procesar el diagnóstico automáticamente.",
                "NORMAL",
                "Te recomendamos visitar el taller para una revisión presencial.",
                List.of()
            );
        }
    }

    private void guardarDiagnostico(DiagnosticoRequestDTO req,
                                     String respuestaRaw,
                                     DiagnosticoResponseDTO response) {
        try {
            Diagnostico d = new Diagnostico();
            d.setFallo(req.descripcionFallo());
            d.setMarca(req.marca());
            d.setModelo(req.modelo());
            d.setAnio(req.anio());
            d.setKilometraje(req.kilometraje());
            d.setRespuestaIa(response.resumenDiagnostico());
            d.setServicioJso(
                objectMapper.writeValueAsString(response.serviciosRecomendados())
            );
            diagnosticoRepo.save(d);
        } catch (Exception e) {
            log.warn("No se pudo guardar el diagnóstico en BD: {}", e.getMessage());
        }
    }

}
