package cl.mecanicontrol.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank(message = "El email es un campo obligatorio")
        @Email(message = "El email no cuenta con un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
