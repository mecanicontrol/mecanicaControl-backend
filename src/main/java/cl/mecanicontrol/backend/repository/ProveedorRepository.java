package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {

    List<Proveedor> findByActivoProveedorTrue();

    List<Proveedor> findByNombreProveedorContainingIgnoreCase(String nombre);
}
