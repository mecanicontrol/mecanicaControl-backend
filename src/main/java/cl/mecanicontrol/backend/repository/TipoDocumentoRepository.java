package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, UUID> {
}
