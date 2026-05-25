package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {

    Optional<OrdenTrabajo> findByCodigoOt(String codigoOt);

    List<OrdenTrabajo> findByTecnicoIdTecnico(UUID tecnicoId);

    List<OrdenTrabajo> findByEstadoOtNombre(String nombre);

    Optional<OrdenTrabajo> findByAgendamientoIdAgendamiento(UUID agendamientoId);

    List<OrdenTrabajo> findByFechaInicioBetween(LocalDateTime desde, LocalDateTime hasta);
}
