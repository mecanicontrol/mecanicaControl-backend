package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilUsuarioRepository extends JpaRepository<PerfilUsuario, UUID> {

    Optional<PerfilUsuario> findByUsuarioId(UUID usuarioId);

}
