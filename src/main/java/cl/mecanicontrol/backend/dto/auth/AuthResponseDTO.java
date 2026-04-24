package cl.mecanicontrol.backend.dto.auth;

import java.util.UUID;

public record AuthResponseDTO(
        String token,
        String tipo,
        String rol,
        UUID usuarioId,
        String nombre
) {
    public AuthResponseDTO(String token, String rol, UUID usuarioId, String nombre) {
        this(token, "Bearer", rol, usuarioId, nombre);
    }
}
