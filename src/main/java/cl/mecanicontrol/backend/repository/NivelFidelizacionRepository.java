package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.NivelFidelizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NivelFidelizacionRepository extends JpaRepository<NivelFidelizacion, UUID> {
}
