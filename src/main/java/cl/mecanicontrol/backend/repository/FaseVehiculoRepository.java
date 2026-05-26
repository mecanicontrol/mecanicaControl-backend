package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.FaseVehiculo;

@Repository
public interface FaseVehiculoRepository extends JpaRepository<FaseVehiculo, UUID> {
    List<FaseVehiculo> findByOrdenTrabajo_Id(UUID ordenTrabajoId);
}