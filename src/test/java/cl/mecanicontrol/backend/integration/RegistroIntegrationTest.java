package cl.mecanicontrol.backend.integration;

import cl.mecanicontrol.backend.dto.auth.RegisterRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegistroIntegrationTest {

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

    // ── TI-03 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-03: POST /api/auth/register con email único retorna 201")
    void register_emailUnico_retorna201() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Nuevo", "Usuario", "nuevo.registro@test.cl", "Admin123!", "CLIENTE"
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity("/api/auth/register", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("mensaje").toString()).contains("correo");
    }

    // ── TI-04 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TI-04: POST /api/auth/register con email duplicado retorna error")
    void register_emailDuplicado_retornaError() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Duplicado", "Test", "duplicado@test.cl", "Admin123!", "CLIENTE"
        );

        // Primer registro — debe pasar
        restTemplate.postForEntity("/api/auth/register", request, Map.class);

        // Segundo registro con mismo email — debe fallar
        ResponseEntity<Map> segundoIntento =
                restTemplate.postForEntity("/api/auth/register", request, Map.class);

        assertThat(segundoIntento.getStatusCode().value())
                .isIn(400, 409, 500); // el backend lanza RuntimeException
        assertThat(segundoIntento.getStatusCode().is2xxSuccessful()).isFalse();
    }
}