package cl.mecanicontrol.backend.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateDTO(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String passwordActual,

        @Size(min = 8, message = "La nueva contraseña debe tener mínimo 8 caracteres")
        @NotBlank(message = "La nueva contraseña es obligatoria")
        String passwordNuevo
) {}
