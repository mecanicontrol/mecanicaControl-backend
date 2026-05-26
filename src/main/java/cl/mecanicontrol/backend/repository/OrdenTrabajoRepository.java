package cl.mecanicontrol.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.OrdenTrabajo;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {
    Optional<OrdenTrabajo> findByCodigoOt(String codigoOt);
}