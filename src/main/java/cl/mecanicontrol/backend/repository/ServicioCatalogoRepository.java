package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.ServicioCatalogo;


@Repository
public interface ServicioCatalogoRepository extends JpaRepository<ServicioCatalogo, UUID>{
    List<ServicioCatalogo> findByServicioActivoTrue();

}
