package cl.mecanicontrol.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.CategoriaServicio;

@Repository

public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, UUID> {

}
