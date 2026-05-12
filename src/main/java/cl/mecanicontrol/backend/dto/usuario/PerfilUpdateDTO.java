package cl.mecanicontrol.backend.dto.usuario;

public record PerfilUpdateDTO(
        String telefono,
        String direccion,
        String rut,
        String fotoUrl
) {}
