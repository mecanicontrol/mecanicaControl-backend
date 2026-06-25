package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.VerificacionEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificacionEmailRepository extends JpaRepository<VerificacionEmail, UUID> {
    Optional<VerificacionEmail> findByToken(String token);
}
