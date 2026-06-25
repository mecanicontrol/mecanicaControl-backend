package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.MensajeOT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MensajeOTRepository extends JpaRepository<MensajeOT, UUID> {
    List<MensajeOT> findByOrdenTrabajoIdOrderByCreadoAtAsc(UUID otId);
}