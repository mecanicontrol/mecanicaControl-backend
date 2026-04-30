package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.Cotizacion;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, UUID> {
    List<Cotizacion> findByClienteId(UUID clienteId);
}
