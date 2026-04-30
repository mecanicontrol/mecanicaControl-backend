package cl.mecanicontrol.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamientoRequestDTO(
    UUID idVehiculo,
    UUID idServicio,
    LocalDateTime fechaInicio,
    String notaCliente,
    String patente
    
){}
