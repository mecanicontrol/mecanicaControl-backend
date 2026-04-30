package cl.mecanicontrol.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import cl.mecanicontrol.backend.entity.EstadoAgendamiento;

public record AgendamientoResponsedDTO(
    UUID idAgendamiento,
    UUID idVehiculo,
    UUID idServicio,
    EstadoAgendamiento estadoAgendamiento,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    Integer precioAcordado,
    String nombreCliente,
    String emailCliente,
    Integer telefonoCliente,
    String patenteVehiculo,
    String marcaVehiculo,
    String modeloVehiculo,
    Integer anioVehiculo,
    String nombreTecnico,
    String nombreServicio,
    LocalDateTime createdAtAgendamiento

) {}
