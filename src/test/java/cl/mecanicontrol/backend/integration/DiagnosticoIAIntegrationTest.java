package cl.mecanicontrol.backend.integration;

import cl.mecanicontrol.backend.dto.DiagnosticoRequestDTO;
import cl.mecanicontrol.backend.dto.DiagnosticoResponseDTO;
import cl.mecanicontrol.backend.service.GroqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * TI-13 — Integración del cotizador con IA.
 * GroqService se mockea con @MockBean para no depender de la API externa en CI/CD.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DiagnosticoIAIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("mecanicontrol_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", () ->
                postgres.getJdbcUrl() + "?prepareThreshold=0");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    // @MockBean reemplaza el bean real de GroqService en el contexto Spring
    // para que el test no llame a la API de Groq real
    @MockBean
    private GroqService groqService;

    private static final String JSON_RESPUESTA_IA = """
            {
              "resumenDiagnostico": "Posible desgaste de pastillas de freno",
              "nivelUrgencia": "URGENTE",
              "recomendacionGeneral": "Revisar sistema de frenos a la brevedad",
              "serviciosRecomendados": [
                {
                  "nombre": "Revisión de frenos",
                  "descripcion": "Inspección y reemplazo de pastillas",
                  "probabilidad": "ALTA",
                  "precioBase": 45000,
                  "urgencia": "URGENTE"
                }
              ]
            }
            """;

    // ── TI-13 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-13: POST api/ia/diagnosticar con descripción válida retorna 200 + serviciosRecomendados")
    void diagnosticarIA_descripcionValida_retorna200ConServicios() {
        // Mockear GroqService para devolver JSON válido sin llamar a la API real
        when(groqService.llamar(anyString())).thenReturn(JSON_RESPUESTA_IA);

        DiagnosticoRequestDTO request = new DiagnosticoRequestDTO(
                "El auto hace ruido al frenar y vibra el pedal",
                "Toyota", "Corolla", (short) 2019, 80000, "AB1234"
        );

        ResponseEntity<DiagnosticoResponseDTO> response = restTemplate.postForEntity(
                "/api/ia/diagnosticar", request, DiagnosticoResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().serviciosRecomendados()).isNotNull();
    }

    @Test
    @DisplayName("TI-13b: POST api/ia/diagnosticar con descripción vacía retorna 400 (validación Bean)")
    void diagnosticarIA_descripcionVacia_retorna400() {
        DiagnosticoRequestDTO request = new DiagnosticoRequestDTO(
                "", // @NotBlank fallará aquí
                "Toyota", "Corolla", (short) 2019, 80000, "AB1234"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/ia/diagnosticar", request, Map.class
        );

        assertThat(response.getStatusCode().value()).isIn(400, 422);
    }
}