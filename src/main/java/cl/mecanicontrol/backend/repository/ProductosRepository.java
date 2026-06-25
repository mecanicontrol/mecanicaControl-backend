package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, UUID> {
    List<Productos> findTop20ByNombreProductoContainingIgnoreCaseAndProductoActivoTrue(String nombre);
}
