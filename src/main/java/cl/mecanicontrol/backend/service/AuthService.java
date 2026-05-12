package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.auth.AuthRequestDTO;
import cl.mecanicontrol.backend.dto.auth.AuthResponseDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterConVehiculoRequestDTO;
import cl.mecanicontrol.backend.dto.auth.RegisterRequestDTO;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.entity.NivelFidelizacion;
import cl.mecanicontrol.backend.entity.PerfilUsuario;
import cl.mecanicontrol.backend.entity.Rol;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.NivelFidelizacionRepository;
import cl.mecanicontrol.backend.repository.PerfilUsuarioRepository;
import cl.mecanicontrol.backend.repository.RolRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.repository.VehiculoRepository;
import cl.mecanicontrol.backend.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final NivelFidelizacionRepository nivelFidelizacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PerfilUsuarioRepository perfilUsuarioRepository,
                       RolRepository rolRepository,
                       ClienteRepository clienteRepository,
                       NivelFidelizacionRepository nivelFidelizacionRepository,
                       VehiculoRepository vehiculoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.rolRepository = rolRepository;
        this.clienteRepository = clienteRepository;
        this.nivelFidelizacionRepository = nivelFidelizacionRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDTO(
                token,
                usuario.getRol().getNombre(),
                usuario.getId(),
                usuario.getNombre()
        );
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request){
        if(usuarioRepository.existsByEmail(request.email())){
            throw new RuntimeException("El email ya se encuentra registrado");
        }

        Rol rol = rolRepository.findByNombre(request.rolNombre())
                .orElseThrow(() -> new RuntimeException("Rol " + request.rolNombre() + " no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuario(usuario);
        perfilUsuarioRepository.save(perfil);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDTO(
                token,
                usuario.getRol().getNombre(),
                usuario.getId(),
                usuario.getNombre()
        );
    }

    @Transactional
    public AuthResponseDTO registerConVehiculo(RegisterConVehiculoRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("El email ya se encuentra registrado");
        }

        Rol rol = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuario(usuario);
        perfil.setTelefono(request.telefono());
        perfilUsuarioRepository.save(perfil);

        NivelFidelizacion nivel = nivelFidelizacionRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay niveles de fidelización configurados"));

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setNivelFidelizacion(nivel);
        cliente.setPuntosFidelizacion(0);
        clienteRepository.save(cliente);

        if (request.patente() != null && !request.patente().isBlank()) {
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setClienteId(cliente.getId());
            vehiculo.setPatente(request.patente().toUpperCase().trim());
            if (request.marcaId() != null && !request.marcaId().isBlank()) {
                vehiculo.setMarcaVehiculoId(UUID.fromString(request.marcaId()));
            }
            if (request.modeloId() != null && !request.modeloId().isBlank()) {
                vehiculo.setModeloVehiculoId(UUID.fromString(request.modeloId()));
            }
            if (request.anio() != null) {
                vehiculo.setAnio(request.anio().shortValue());
            }
            if (request.kilometraje() != null) {
                vehiculo.setKilometrajeIngreso(request.kilometraje());
            }
            vehiculoRepository.save(vehiculo);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponseDTO(
                token,
                usuario.getRol().getNombre(),
                usuario.getId(),
                usuario.getNombre()
        );
    }
}
