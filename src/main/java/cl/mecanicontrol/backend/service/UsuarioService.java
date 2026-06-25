package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.usuario.PasswordUpdateDTO;
import cl.mecanicontrol.backend.dto.usuario.PerfilUpdateDTO;
import cl.mecanicontrol.backend.dto.usuario.UsuarioResponseDTO;
import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final NivelFidelizacionRepository nivelFidelizacionRepository;
    private final NivelTecnicoRepository nivelTecnicoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PerfilUsuarioRepository perfilUsuarioRepository,
                          PasswordEncoder passwordEncoder,
                          RolRepository rolRepository,
                          ClienteRepository clienteRepository,
                          TecnicoRepository tecnicoRepository,
                          NivelFidelizacionRepository nivelFidelizacionRepository,
                          NivelTecnicoRepository nivelTecnicoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
        this.clienteRepository = clienteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.nivelFidelizacionRepository = nivelFidelizacionRepository;
        this.nivelTecnicoRepository = nivelTecnicoRepository;
    }

    public UsuarioResponseDTO getMiPerfil(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        PerfilUsuario perfil = perfilUsuarioRepository.findByUsuarioId(usuarioId).orElse(null);
        return toDTOConPerfil(usuario, perfil);
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional //@Transactional: Si no se ejecuta o falla, se revierte
    public PerfilUsuario updatePerfil(UUID usuarioId, PerfilUpdateDTO dto) {
        PerfilUsuario perfil = perfilUsuarioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado"));

        if (dto.telefono() != null) perfil.setTelefono(dto.telefono());
        if (dto.direccion() != null) perfil.setDireccion(dto.direccion());
        if (dto.rut() != null) perfil.setRut(dto.rut());
        if (dto.fotoUrl() != null) perfil.setFotoUrl(dto.fotoUrl());

        return perfilUsuarioRepository.save(perfil);
    }

    @Transactional
    public void cambiarPassword(UUID usuarioId, PasswordUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(dto.passwordActual(), usuario.getPasswordHash())){
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(dto.passwordNuevo()));
        usuarioRepository.save(usuario);
    }

    // ─── Admin: crear usuario ─────────────────────────────────────────────────
    @Transactional
    public UsuarioResponseDTO crearUsuarioAdmin(String nombre, String apellido, String email,
                                                String password, String rolNombre, String telefono) {
        if (usuarioRepository.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado: " + rolNombre));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido != null ? apellido : "");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuario(usuario);
        if (telefono != null) perfil.setTelefono(telefono);
        perfilUsuarioRepository.save(perfil);

        crearPerfilRol(usuario, rolNombre);

        return toDTO(usuario);
    }

    // ─── Admin: actualizar usuario ────────────────────────────────────────────
    @Transactional
    public UsuarioResponseDTO actualizarUsuarioAdmin(UUID id, String nombre, String apellido,
                                                     String email, String rolNombre) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (nombre != null) usuario.setNombre(nombre);
        if (apellido != null) usuario.setApellido(apellido);
        if (email != null && !email.equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(email))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
            usuario.setEmail(email);
        }
        boolean rolCambio = false;
        if (rolNombre != null && !rolNombre.equals(usuario.getRol().getNombre())) {
            Rol rol = rolRepository.findByNombre(rolNombre)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado: " + rolNombre));
            usuario.setRol(rol);
            rolCambio = true;
        }

        usuarioRepository.save(usuario);

        if (rolCambio && rolNombre != null)
            crearPerfilRol(usuario, rolNombre);

        return toDTO(usuario);
    }

    // ─── Admin: activar / desactivar ──────────────────────────────────────────
    @Transactional
    public void toggleActivo(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setActivo(!usuario.isActivo());
        usuarioRepository.save(usuario);
    }

    // ─── Admin: cambiar contraseña (sin validar contraseña actual) ────────────
    @Transactional
    public void cambiarPasswordAdmin(UUID id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    // ─── Crea el registro de perfil de rol si no existe ───────────────────────
    private void crearPerfilRol(Usuario usuario, String rolNombre) {
        if ("CLIENTE".equals(rolNombre) && clienteRepository.findByUsuarioId(usuario.getId()).isEmpty()) {
            NivelFidelizacion nivel = nivelFidelizacionRepository.findAll().stream()
                    .findFirst().orElse(null);
            if (nivel != null) {
                Cliente cliente = new Cliente();
                cliente.setUsuario(usuario);
                cliente.setNivelFidelizacion(nivel);
                clienteRepository.save(cliente);
            }
        } else if ("TECNICO".equals(rolNombre) && tecnicoRepository.findByIdUsuarioId(usuario.getId()).isEmpty()) {
            NivelTecnico nivel = nivelTecnicoRepository.findAll().stream().findFirst().orElse(null);
            if (nivel == null) return;
            Tecnico tecnico = new Tecnico();
            tecnico.setIdUsuario(usuario);
            tecnico.setIdNivelTecnico(nivel);
            tecnico.setDisponible(true);
            tecnico.setHorasSemanales(40);
            tecnicoRepository.save(tecnico);
        }
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                null, null, null, null
        );
    }

    private UsuarioResponseDTO toDTOConPerfil(Usuario usuario, PerfilUsuario perfil) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                perfil != null ? perfil.getTelefono() : null,
                perfil != null ? perfil.getRut() : null,
                perfil != null ? perfil.getDireccion() : null,
                perfil != null ? perfil.getFotoUrl() : null
        );
    }
}
