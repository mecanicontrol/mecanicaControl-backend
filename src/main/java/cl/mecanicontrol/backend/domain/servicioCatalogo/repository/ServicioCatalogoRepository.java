package cl.mecanicontrol.backend.domain.servicioCatalogo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.domain.servicioCatalogo.entity.ServicioCatalogo;


@Repository
public interface ServicioCatalogoRepository extends JpaRepository<ServicioCatalogo, UUID>{
    List<ServicioCatalogo> findByServicioActivoTrue();

}
