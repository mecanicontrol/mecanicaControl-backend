package cl.mecanicontrol.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import cl.mecanicontrol.backend.dto.AgendamientoRequestDTO;
import cl.mecanicontrol.backend.dto.AgendamientoResponsedDTO;
import cl.mecanicontrol.backend.dto.DisponibilidadSlotDTO;

public interface AgendamientoService {

    AgendamientoResponsedDTO crear(AgendamientoRequestDTO request);

    List<AgendamientoResponsedDTO> findAll(String estado, LocalDate fecha);

    List<AgendamientoResponsedDTO> findByCliente(UUID clienteId);

    List<AgendamientoResponsedDTO> findByTecnico(UUID tecnicoId);

    AgendamientoResponsedDTO confirmar(UUID id, UUID tecnicoId);

    AgendamientoResponsedDTO cancelar(UUID id);

    List<DisponibilidadSlotDTO> getDisponibilidad(LocalDate fecha, UUID servicioId);
}
