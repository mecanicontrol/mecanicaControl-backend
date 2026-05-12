package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.usuario.PasswordUpdateDTO;
import cl.mecanicontrol.backend.dto.usuario.PerfilUpdateDTO;
import cl.mecanicontrol.backend.dto.usuario.UsuarioResponseDTO;
import cl.mecanicontrol.backend.entity.PerfilUsuario;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.repository.PerfilUsuarioRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PerfilUsuarioRepository perfilUsuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponseDTO getMiPerfil(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDTO(usuario);
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

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.isActivo()
        );
    }
}
