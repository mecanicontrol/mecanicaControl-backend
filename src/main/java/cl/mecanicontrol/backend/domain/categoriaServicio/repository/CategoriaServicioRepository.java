package cl.mecanicontrol.backend.domain.categoriaServicio.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.domain.categoriaServicio.entity.CategoriaServicio;

@Repository

public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, UUID> {

}
