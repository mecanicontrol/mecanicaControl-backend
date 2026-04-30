package cl.mecanicontrol.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;

@Repository
public interface MarcaVehiculoRepository extends JpaRepository<MarcaVehiculo, UUID> {}
