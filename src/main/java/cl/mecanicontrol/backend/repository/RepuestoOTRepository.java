package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.RepuestoOT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RepuestoOTRepository extends JpaRepository<RepuestoOT, UUID> {
    List<RepuestoOT> findByOrdenTrabajoIdOrderByCreadoAtAsc(UUID otId);
}
