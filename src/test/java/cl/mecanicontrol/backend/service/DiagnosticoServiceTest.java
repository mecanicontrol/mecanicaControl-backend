package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.DiagnosticoRequestDTO;
import cl.mecanicontrol.backend.dto.DiagnosticoResponseDTO;
import cl.mecanicontrol.backend.entity.ServicioCatalogo;
import cl.mecanicontrol.backend.repository.DiagnosticoRepository;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoServiceTest {

    @Mock  GroqService groqService;
    @Mock  ServicioCatalogoRepository servicioRepo;
    @Mock  DiagnosticoRepository diagnosticoRepo;
    @Spy   ObjectMapper objectMapper;

    @InjectMocks DiagnosticoServiceImpl diagnosticoService;

    private DiagnosticoRequestDTO requestValido;
    private ServicioCatalogo svcActivo1;
    private ServicioCatalogo svcActivo2;
    private ServicioCatalogo svcInactivo;

    private static final String JSON_VALIDO = """
        {
          "resumenDiagnostico": "Frenos desgastados",
          "nivelUrgencia": "URGENTE",
          "recomendacionGeneral": "Revisar frenos de inmediato",
          "serviciosRecomendados": [
            {
              "nombre": "Cambio de frenos",
              "descripcion": "Pastillas bajo el límite de seguridad",
              "probabilidad": "ALTA",
              "precioBase": 45000,
              "urgencia": "URGENTE"
            }
          ]
        }
        """;

    @BeforeEach
    void setUp() {
        requestValido = new DiagnosticoRequestDTO(
            "El auto frena mal y hace ruido al frenar",
            "Toyota", "Corolla", (short) 2019, 80000, "ABCD12"
        );

        svcActivo1 = new ServicioCatalogo();
        svcActivo1.setNombreServicio("Cambio de frenos");
        svcActivo1.setPrecioBase(BigDecimal.valueOf(45000));
        svcActivo1.setDuracion(90);
        svcActivo1.setServicioActivo(true);

        svcActivo2 = new ServicioCatalogo();
        svcActivo2.setNombreServicio("Revisión de suspensión");
        svcActivo2.setPrecioBase(BigDecimal.valueOf(30000));
        svcActivo2.setDuracion(60);
        svcActivo2.setServicioActivo(true);

        svcInactivo = new ServicioCatalogo();
        svcInactivo.setNombreServicio("Servicio descontinuado");
        svcInactivo.setPrecioBase(BigDecimal.valueOf(10000));
        svcInactivo.setDuracion(30);
        svcInactivo.setServicioActivo(false);
    }

    // ── TU-DIA-01 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-DIA-01: prompt enviado a GroqService incluye nombres de servicios del catálogo")
    void diagnosticar_promptIncluyeCatalogoReal() {
        // Arrange
        when(servicioRepo.findByServicioActivoTrue()).thenReturn(List.of(svcActivo1, svcActivo2));
        when(groqService.llamar(anyString())).thenReturn(JSON_VALIDO);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        diagnosticoService.diagnosticar(requestValido);

        // Assert — el prompt debe contener los nombres de servicios activos
        verify(groqService).llamar(promptCaptor.capture());
        String promptEnviado = promptCaptor.getValue();
        assertThat(promptEnviado).contains("Cambio de frenos");
        assertThat(promptEnviado).contains("Revisión de suspensión");
    }

    // ── TU-DIA-02 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-DIA-02: respuesta JSON válida de IA se parsea correctamente")
    void diagnosticar_respuestaValidaIA_parseaCorrectamente() {
        // Arrange
        when(servicioRepo.findByServicioActivoTrue()).thenReturn(List.of(svcActivo1));
        when(groqService.llamar(anyString())).thenReturn(JSON_VALIDO);

        // Act
        DiagnosticoResponseDTO result = diagnosticoService.diagnosticar(requestValido);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.resumenDiagnostico()).isEqualTo("Frenos desgastados");
        assertThat(result.nivelUrgencia()).isEqualTo("URGENTE");
        assertThat(result.serviciosRecomendados()).isNotEmpty();
        assertThat(result.serviciosRecomendados().get(0).nombre()).isEqualTo("Cambio de frenos");
    }

    // ── TU-DIA-03 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-DIA-03: respuesta malformada de Groq devuelve respuesta de fallback")
    void diagnosticar_respuestaMalformada_devuelveFallback() {
        // Arrange
        when(servicioRepo.findByServicioActivoTrue()).thenReturn(List.of(svcActivo1));
        when(groqService.llamar(anyString())).thenReturn("esto no es JSON válido {{{");

        // Act
        DiagnosticoResponseDTO result = diagnosticoService.diagnosticar(requestValido);

        // Assert — el servicio retorna fallback en lugar de lanzar excepción
        assertThat(result).isNotNull();
        assertThat(result.serviciosRecomendados()).isEmpty();
        assertThat(result.resumenDiagnostico()).contains("No se pudo procesar");
    }

    // ── TU-DIA-04 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-DIA-04: prompt solo incluye servicios activos, filtra inactivos")
    void diagnosticar_filtraServiciosInactivos() {
        // Arrange — findByServicioActivoTrue devuelve SOLO activos (el repo ya filtra)
        when(servicioRepo.findByServicioActivoTrue()).thenReturn(List.of(svcActivo1));
        when(groqService.llamar(anyString())).thenReturn(JSON_VALIDO);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        diagnosticoService.diagnosticar(requestValido);

        // Assert — el servicio inactivo NO debe aparecer en el prompt
        verify(groqService).llamar(promptCaptor.capture());
        assertThat(promptCaptor.getValue()).doesNotContain("Servicio descontinuado");
        assertThat(promptCaptor.getValue()).contains("Cambio de frenos");
    }

    // ── TU-DIA-05 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-DIA-05: error en GroqService devuelve respuesta de fallback amigable")
    void diagnosticar_falloGroq_devuelveFallbackAmigable() {
        // Arrange
        when(servicioRepo.findByServicioActivoTrue()).thenReturn(List.of(svcActivo1));
        when(groqService.llamar(anyString())).thenThrow(new RuntimeException("Connection timeout"));

        // Act
        DiagnosticoResponseDTO result = diagnosticoService.diagnosticar(requestValido);

        // Assert — no lanza excepción, devuelve fallback
        assertThat(result).isNotNull();
        assertThat(result.serviciosRecomendados()).isEmpty();
    }
}
