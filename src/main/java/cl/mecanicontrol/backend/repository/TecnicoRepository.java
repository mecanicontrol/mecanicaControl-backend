package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.Tecnico;

@Repository
public interface TecnicoRepository extends JpaRepository<Tecnico, UUID> {
    List<Tecnico> findByDisponibleTrue();
}
