package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, UUID> {
    List<MovimientoInventario> findByProductoIdProductoOrderByCreatedAtDesc(UUID productoId);
}
