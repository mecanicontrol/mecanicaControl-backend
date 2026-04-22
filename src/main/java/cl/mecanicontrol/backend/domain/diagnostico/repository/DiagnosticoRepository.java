package cl.mecanicontrol.backend.domain.diagnostico.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.domain.diagnostico.entity.Diagnostico;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, UUID>{

}
