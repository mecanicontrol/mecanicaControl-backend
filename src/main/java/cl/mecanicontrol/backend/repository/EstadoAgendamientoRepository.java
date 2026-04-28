package cl.mecanicontrol.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.EstadoAgendamiento;

@Repository
public interface EstadoAgendamientoRepository extends JpaRepository<EstadoAgendamiento, UUID> {
    Optional<EstadoAgendamiento> findByNombre(String nombre);
}
