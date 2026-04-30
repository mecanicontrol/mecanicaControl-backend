package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.CondicionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CondicionPagoRepository extends JpaRepository<CondicionPago, UUID> {
}
