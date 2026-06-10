package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.EstadoPago;
import cl.mecanicontrol.backend.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstadoPagoRepository extends JpaRepository<EstadoPago, UUID> {

    Optional<EstadoPago> findByNombre(String nombre);
}
