package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.EstadoAgendamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EstadoAgendamientoRepository extends JpaRepository<EstadoAgendamiento, UUID> {
}
