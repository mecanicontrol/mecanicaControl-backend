package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, UUID> {
}
