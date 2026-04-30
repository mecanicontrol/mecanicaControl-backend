package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.EstadoOt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EstadoOtRepository extends JpaRepository<EstadoOt, UUID> {
}
