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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {

    // ── Contenedor PostgreSQL compartido para todos los tests ─────────────────

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
        .withDatabaseName("mecanicontrol_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Supabase usa prepareThreshold=0 en prod; aquí usamos JDBC directo
        registry.add("spring.datasource.url", () ->
            postgres.getJdbcUrl() + "?prepareThreshold=0");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    // Credenciales de test creadas en @BeforeAll
    private static final String ADMIN_EMAIL    = "admin.test@mecanicontrol.cl";
    private static final String ADMIN_PASSWORD = "Admin123!";
    private static final String CLIENTE_EMAIL  = "cliente.test@mecanicontrol.cl";
    private static final String CLIENTE_PASSWORD = "Cliente123!";

    /**
     * Registra un admin y un cliente antes de todos los tests.
     * Usamos la propia API de registro para crear usuarios con contraseñas conocidas,
     * sin depender de los hashes placeholder del seed de producción.
     */
    @BeforeAll
    void crearUsuariosDePrueba() {
        // Registrar administrador
        RegisterRequestDTO adminReq = new RegisterRequestDTO(
            "Admin", "Test", ADMIN_EMAIL, ADMIN_PASSWORD, "ADMIN"
        );
        restTemplate.postForEntity("/api/auth/register", adminReq, AuthResponseDTO.class);

        // Registrar cliente
        RegisterRequestDTO clienteReq = new RegisterRequestDTO(
            "Cliente", "Test", CLIENTE_EMAIL, CLIENTE_PASSWORD, "CLIENTE"
        );
        restTemplate.postForEntity("/api/auth/register", clienteReq, AuthResponseDTO.class);
    }

    // ── TI-AUTH-01 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-AUTH-01: POST /api/auth/login con credenciales válidas retorna 200 + token")
    void postLogin_credencialesValidas_retorna200ConToken() {
        AuthRequestDTO request = new AuthRequestDTO(ADMIN_EMAIL, ADMIN_PASSWORD);

        ResponseEntity<AuthResponseDTO> response =
            restTemplate.postForEntity("/api/auth/login", request, AuthResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
    }

    // ── TI-AUTH-02 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-AUTH-02: POST /api/auth/login con password incorrecta retorna 401")
    void postLogin_passwordIncorrecta_retorna401() {
        AuthRequestDTO request = new AuthRequestDTO(ADMIN_EMAIL, "wrong-password");

        ResponseEntity<Map> response =
            restTemplate.postForEntity("/api/auth/login", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── TI-AUTH-03 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-AUTH-03: GET /api/admin/dashboard sin token retorna 401")
    void getDashboard_sinToken_retorna401() {
        ResponseEntity<Map> response =
            restTemplate.getForEntity("/api/admin/dashboard", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── TI-AUTH-04 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-AUTH-04: GET /api/admin/dashboard con token de CLIENTE retorna 403")
    void getDashboard_conTokenCliente_retorna403() {
        // Obtener token de cliente
        AuthRequestDTO loginReq = new AuthRequestDTO(CLIENTE_EMAIL, CLIENTE_PASSWORD);
        ResponseEntity<AuthResponseDTO> loginRes =
            restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponseDTO.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginRes.getBody().token();

        // Llamar al dashboard con token de cliente
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
            restTemplate.exchange("/api/admin/dashboard", HttpMethod.GET, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── TI-AUTH-05 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-AUTH-05: GET /api/admin/dashboard con token de ADMIN retorna 200")
    void getDashboard_conTokenAdmin_retorna200() {
        // Obtener token de admin
        AuthRequestDTO loginReq = new AuthRequestDTO(ADMIN_EMAIL, ADMIN_PASSWORD);
        ResponseEntity<AuthResponseDTO> loginRes =
            restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponseDTO.class);

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginRes.getBody().token();

        // Llamar al dashboard con token de admin
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
            restTemplate.exchange("/api/admin/dashboard", HttpMethod.GET, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}