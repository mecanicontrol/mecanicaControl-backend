package cl.mecanicontrol.backend.integration;

import cl.mecanicontrol.backend.dto.auth.AuthRequestDTO;
import cl.mecanicontrol.backend.dto.auth.AuthResponseDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterRequestDTO;
import cl.mecanicontrol.backend.dto.ot.OTRequestDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrdenTrabajoIntegrationTest {

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

    private String tokenAdmin;

    @BeforeAll
    void crearAdmin() {
        RegisterRequestDTO adminReq = new RegisterRequestDTO(
                "Admin", "OT", "admin.ot@test.cl", "Admin123!", "ADMIN"
        );
        restTemplate.postForEntity("/api/auth/register", adminReq, Map.class);

        // intentar login (usuario inactivo hasta verificar — se espera 401 en test)
        // usamos el token solo si el sistema activa automáticamente en perfil test
        ResponseEntity<AuthResponseDTO> loginRes = restTemplate.postForEntity(
                "/api/auth/login",
                new AuthRequestDTO("admin.ot@test.cl", "Admin123!"),
                AuthResponseDTO.class
        );
        if (loginRes.getStatusCode() == HttpStatus.OK && loginRes.getBody() != null) {
            tokenAdmin = loginRes.getBody().token();
        }
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // ── TI-08 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-08: POST /api/ot sin token retorna 401 (endpoint protegido ADMIN)")
    void crearOT_sinToken_retorna401() {
        OTRequestDTO body = new OTRequestDTO(UUID.randomUUID(), null);

        ResponseEntity<Map> response =
                restTemplate.postForEntity("/api/ot", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── TI-09 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-09: GET /api/seguimiento/{codigo} con código inexistente retorna 404")
    void getSeguimiento_codigoInexistente_retorna404() {
        // El endpoint de seguimiento es público pero retorna 404 si no existe la OT
        ResponseEntity<Map> response =
                restTemplate.getForEntity("/api/seguimiento/OT-9999-9999", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}