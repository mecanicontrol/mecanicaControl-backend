package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Productos, UUID> {

    List<Productos> findByProductoActivoTrue();

    Optional<Productos> findBySku(String sku);

    @Query("SELECT p FROM Productos p WHERE p.stockActual <= p.stockMinimo")
    List<Productos> findByStockActualLessThanEqualStockMinimo();
}
