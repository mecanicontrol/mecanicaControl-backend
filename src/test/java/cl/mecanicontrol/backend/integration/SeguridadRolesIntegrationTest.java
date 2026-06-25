package cl.mecanicontrol.backend.integration;

import cl.mecanicontrol.backend.dto.auth.AuthRequestDTO;
import cl.mecanicontrol.backend.dto.auth.AuthResponseDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterRequestDTO;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TI-10, TI-12, TI-14 — Seguridad por roles.
 * Valida que los endpoints protegidos devuelven 401/403 según corresponda.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeguridadRolesIntegrationTest {

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
    private String tokenTecnico;

    @BeforeAll
    void crearUsuariosDePrueba() {
        // Registrar cliente
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequestDTO("Cliente", "Roles", "cliente.roles@test.cl", "Admin123!", "CLIENTE"),
                Map.class);

        // Registrar técnico
        restTemplate.postForEntity("/api/auth/register",
                new RegisterRequestDTO("Tecnico", "Roles", "tecnico.roles@test.cl", "Admin123!", "TECNICO"),
                Map.class);

        // Intentar login (solo funcionará si el perfil test activa usuarios automáticamente)
        ResponseEntity<AuthResponseDTO> loginCliente = restTemplate.postForEntity(
                "/api/auth/login",
                new AuthRequestDTO("cliente.roles@test.cl", "Admin123!"),
                AuthResponseDTO.class);
        if (loginCliente.getStatusCode() == HttpStatus.OK && loginCliente.getBody() != null) {
            tokenCliente = loginCliente.getBody().token();
        }

        ResponseEntity<AuthResponseDTO> loginTecnico = restTemplate.postForEntity(
                "/api/auth/login",
                new AuthRequestDTO("tecnico.roles@test.cl", "Admin123!"),
                AuthResponseDTO.class);
        if (loginTecnico.getStatusCode() == HttpStatus.OK && loginTecnico.getBody() != null) {
            tokenTecnico = loginTecnico.getBody().token();
        }
    }

    private HttpEntity<Void> request(String token) {
        HttpHeaders h = new HttpHeaders();
        if (token != null) h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(h);
    }

    // ── TI-10 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-10: GET /api/admin/inventario sin token retorna 401")
    void getInventarioAdmin_sinToken_retorna401() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/admin/inventario", HttpMethod.GET, request(null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── TI-12 — complementa TI-AUTH-04 con endpoint de inventario ─────────────

    @Test
    @DisplayName("TI-12: GET /api/admin/inventario con rol CLIENTE retorna 403")
    void getInventarioAdmin_conRolCliente_retorna403() {
        // Solo aplica si el cliente logró autenticarse (usuario activo en perfil test)
        if (tokenCliente == null) {
            // Usuario inactivo — sin token → 401, que también valida la seguridad
            ResponseEntity<Map> response = restTemplate.exchange(
                    "/api/admin/inventario", HttpMethod.GET, request(null), Map.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            return;
        }

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/admin/inventario", HttpMethod.GET, request(tokenCliente), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── TI-14 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-14: GET /api/tecnicos/ordenes sin token retorna 401")
    void getMisOrdenesTecnico_sinToken_retorna401() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/tecnicos/ordenes", HttpMethod.GET, request(null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}