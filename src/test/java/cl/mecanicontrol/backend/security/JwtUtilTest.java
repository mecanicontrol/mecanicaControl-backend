package cl.mecanicontrol.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    // Clave larga (>= 32 chars) para satisfacer el mínimo de 256 bits de HS256
    private static final String SECRET =
        "mecanicontrol-test-secret-key-at-least-256-bits-long-padded-ok";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hora

    private JwtUtil jwtUtil;
    private UserDetails userDetailsAdmin;
    private UserDetails userDetailsUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_MS);

        userDetailsAdmin = User.withUsername("test@test.com")
            .password("ignored")
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
            .build();

        userDetailsUser = User.withUsername("user@test.com")
            .password("ignored")
            .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
            .build();
    }

    // ── TU-JWT-01 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-JWT-01: generateToken retorna string con tres partes separadas por punto")
    void generarToken_retornaStringConTresPartes() {
        // Act
        String token = jwtUtil.generateToken(userDetailsAdmin);

        // Assert — formato JWT: header.payload.signature → exactamente 2 puntos
        assertThat(token).isNotBlank();
        long puntos = token.chars().filter(c -> c == '.').count();
        assertThat(puntos).isEqualTo(2);
    }

    // ── TU-JWT-02 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-JWT-02: validateToken con token válido retorna true")
    void validarToken_tokenValido_retornaTrue() {
        // Arrange
        String token = jwtUtil.generateToken(userDetailsAdmin);

        // Act
        boolean valido = jwtUtil.validateToken(token, userDetailsAdmin);

        // Assert
        assertThat(valido).isTrue();
    }

    // ── TU-JWT-03 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-JWT-03: validateToken con token expirado lanza ExpiredJwtException")
    void validarToken_tokenExpirado_lanzaExpiredJwtException() {
        // Arrange — construir un token ya expirado con la misma clave de firma
        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String tokenExpirado = Jwts.builder()
            .subject("test@test.com")
            .issuedAt(new Date(System.currentTimeMillis() - 20_000))
            .expiration(new Date(System.currentTimeMillis() - 10_000)) // ya expirado
            .signWith(key)
            .compact();

        // Act + Assert — validateToken llama extractUsername → parseSignedClaims → lanza
        assertThrows(ExpiredJwtException.class,
            () -> jwtUtil.validateToken(tokenExpirado, userDetailsAdmin));
    }

    // ── TU-JWT-04 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-JWT-04: extractUsername retorna el email correcto del token")
    void extractUsername_tokenValido_retornaEmailCorrecto() {
        // Arrange
        String token = jwtUtil.generateToken(userDetailsUser);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("user@test.com");
    }

    // ── Extra: token de un usuario no valida para otro ────────────────────────

    @Test
    @DisplayName("validateToken retorna false para usuario distinto al del token")
    void validarToken_usuarioDistinto_retornaFalse() {
        String tokenAdmin = jwtUtil.generateToken(userDetailsAdmin);
        boolean valido = jwtUtil.validateToken(tokenAdmin, userDetailsUser);
        assertThat(valido).isFalse();
    }
}