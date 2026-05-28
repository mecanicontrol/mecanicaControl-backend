package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.Fase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaseRepository extends JpaRepository<Fase, UUID> {
    List<Fase> findAllByOrderByOrdenAsc();
}
