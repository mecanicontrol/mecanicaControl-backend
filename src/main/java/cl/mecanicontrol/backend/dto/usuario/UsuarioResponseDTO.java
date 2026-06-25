package cl.mecanicontrol.backend.dto.usuario;

import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nombre,
        String apellido,
        String email,
        String rol,
        Boolean activo,
        String telefono,
        String rut,
        String direccion,
        String fotoUrl
) {}
