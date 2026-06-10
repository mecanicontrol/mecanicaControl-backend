package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {

    List<Pago> findByOrdenTrabajoId(UUID ordenTrabajoId);

    List<Pago> findByClienteId(UUID clienteId);
}
