package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.Vehiculo;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, UUID> {
    List<Vehiculo> findByClienteId(UUID clienteId);

    java.util.Optional<Vehiculo> findByPatente(String patente);
}
