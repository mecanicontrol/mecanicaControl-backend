package cl.mecanicontrol.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgendamientoResponsedDTO(
    UUID idAgendamiento,
    UUID idVehiculo,
    UUID idServicio,
    String estadoAgendamiento,
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
    LocalDateTime createdAtAgendamiento,
    List<String> nombresServicios
) {}