package cl.mecanicontrol.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgendamientoRequestDTO(
    UUID idVehiculo,
    List<UUID> idServicios,
    LocalDateTime fechaInicio,
    String notaCliente,
    String patente
){}