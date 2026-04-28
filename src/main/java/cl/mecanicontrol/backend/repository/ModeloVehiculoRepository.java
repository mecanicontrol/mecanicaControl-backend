package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.ModeloVehiculo;

@Repository
public interface ModeloVehiculoRepository extends JpaRepository<ModeloVehiculo, UUID> {
    List<ModeloVehiculo> findByMarcaId(UUID marcaId);
}
