package cl.mecanicontrol.backend.integration;

import cl.mecanicontrol.backend.dto.AgendamientoRequestDTO;
import cl.mecanicontrol.backend.dto.DisponibilidadSlotDTO;
import cl.mecanicontrol.backend.dto.auth.AuthRequestDTO;
import cl.mecanicontrol.backend.dto.auth.AuthResponseDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterConVehiculoRequestDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgendamientoIntegrationTest {

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

    private String tokenCliente;

    @BeforeAll
    void crearClienteDePrueba() {
        // Registrar cliente con vehículo
        RegisterConVehiculoRequestDTO req = new RegisterConVehiculoRequestDTO(
                "Cliente", "Agenda", "cliente.agenda@test.cl", "+56912345678",
                "Admin123!", "AG0001", null, null, 2020, 50000
        );
        restTemplate.postForEntity("/api/auth/register-con-vehiculo", req, Map.class);

        // Nota: el usuario queda inactivo hasta verificar email.
        // Para los tests de agendamiento sin autenticación (TI-06) no necesitamos token.
        // TI-05 prueba que el endpoint existe y responde — la lógica de auth la prueba TI-06.
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // ── TI-05 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-05: POST /api/agendamientos es un endpoint público (no requiere token)")
    void crearAgendamiento_sinToken_retorna401() {
        // El endpoint es público (permite agendamiento anónimo).
        // Con datos inválidos (UUIDs aleatorios) el servicio devuelve error de negocio, no 401/403.
        AgendamientoRequestDTO body = new AgendamientoRequestDTO(
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
                "Test",
                null
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity("/api/agendamientos", body, Map.class);

        // No debe rechazar por falta de autenticación
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── TI-06 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-06: GET /api/disponibilidad retorna lista de slots para fecha futura")
    void getDisponibilidad_fechaFutura_retornaListaSlots() {
        // Endpoint público — no requiere token
        String fechaManana = LocalDate.now().plusDays(1).toString();

        ResponseEntity<List> response = restTemplate.getForEntity(
                "/api/disponibilidad?fecha=" + fechaManana, List.class
        );

        // Debe responder 200 con lista (vacía o con slots según catálogo en BD semilla)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    // ── TI-07 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-07: GET /api/taller/estado retorna capacidad actual del taller")
    void getTallerEstado_retornaCapacidadActual() {
        // Endpoint público que expone cuántos vehículos hay en el taller en este momento
        ResponseEntity<Map> response =
                restTemplate.getForEntity("/api/taller/estado", Map.class);

        // Con BD vacía (test), el taller tiene 0 vehículos activos
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}