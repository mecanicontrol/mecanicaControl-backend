package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.auth.AuthRequestDTO;
import cl.mecanicontrol.backend.dto.auth.AuthResponseDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterRequestDTO;
import cl.mecanicontrol.backend.entity.Rol;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.NivelFidelizacionRepository;
import cl.mecanicontrol.backend.repository.PerfilUsuarioRepository;
import cl.mecanicontrol.backend.repository.RolRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.repository.VehiculoRepository;
import cl.mecanicontrol.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PerfilUsuarioRepository perfilUsuarioRepository;
    @Mock RolRepository rolRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock NivelFidelizacionRepository nivelFidelizacionRepository;
    @Mock VehiculoRepository vehiculoRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;

    @InjectMocks
    AuthService authService;

    private Usuario usuarioAdmin;
    private UserDetails userDetails;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        rolAdmin = new Rol();
        rolAdmin.setNombre("ADMIN");

        usuarioAdmin = new Usuario();
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setApellido("Test");
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setPasswordHash("$2a$10$hashedpassword");
        usuarioAdmin.setRol(rolAdmin);

        userDetails = User.withUsername("admin@test.com")
            .password("$2a$10$hashedpassword")
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
            .build();
    }

    // ── TU-AUTH-01 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AUTH-01: login con credenciales válidas retorna token JWT")
    void loginConCredencialesValidas_retornaTokenJwt() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(userDetails);
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuarioAdmin));
        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        // Act
        AuthResponseDTO result = authService.login(new AuthRequestDTO("admin@test.com", "Admin123!"));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("fake-jwt-token");
        assertThat(result.rol()).isEqualTo("ADMIN");
    }

    // ── TU-AUTH-02 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AUTH-02: login con password incorrecto lanza BadCredentialsException")
    void loginConPasswordIncorrecto_lanzaExcepcion() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        assertThrows(BadCredentialsException.class,
            () -> authService.login(new AuthRequestDTO("admin@test.com", "wrong-password")));

        verify(jwtUtil, never()).generateToken(any());
    }

    // ── TU-AUTH-03 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AUTH-03: login con email inexistente lanza excepción")
    void loginConEmailInexistente_lanzaExcepcion() {
        // Arrange — el AuthenticationManager rechaza el usuario desconocido
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("User not found"));

        // Act + Assert
        assertThrows(BadCredentialsException.class,
            () -> authService.login(new AuthRequestDTO("noexiste@test.com", "cualquier")));

        verify(usuarioRepository, never()).findByEmail(anyString());
    }

    // ── TU-AUTH-04 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AUTH-04: registro con email duplicado lanza RuntimeException")
    void registroConEmailDuplicado_lanzaExcepcion() {
        // Arrange
        when(usuarioRepository.existsByEmail("admin@test.com")).thenReturn(true);

        RegisterRequestDTO request = new RegisterRequestDTO(
            "Admin", "Test", "admin@test.com", "Admin123!", "ADMIN"
        );

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.register(request));

        assertThat(ex.getMessage()).contains("email");
        verify(usuarioRepository, never()).save(any());
    }
}