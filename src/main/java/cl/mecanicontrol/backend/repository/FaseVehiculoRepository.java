package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.FaseVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaseVehiculoRepository extends JpaRepository<FaseVehiculo, UUID> {

    List<FaseVehiculo> findByOrdenTrabajoId(UUID otId);

    Optional<FaseVehiculo> findByOrdenTrabajoIdAndFaseId(UUID otId, UUID faseId);

    List<FaseVehiculo> findByTecnicoIdTecnicoAndFinAtIsNotNull(UUID tecnicoId);

    @Query("SELECT fv FROM FaseVehiculo fv JOIN FETCH fv.ordenTrabajo ot JOIN FETCH fv.fase f WHERE f.nombre = 'CONTROL_CALIDAD' AND fv.aprobacionEstado = 'PENDIENTE' AND fv.finAt IS NULL")
    List<FaseVehiculo> findPendingControlCalidad();
}
