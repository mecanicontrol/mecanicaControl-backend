package cl.mecanicontrol.backend.dto.cliente;

import cl.mecanicontrol.backend.dto.usuario.UsuarioResponseDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record ClienteResponseDTO(
        UUID id,
        UsuarioResponseDTO usuario,
        String nivelFidelizacion,
        int puntos,
        BigDecimal descuento
) { }
