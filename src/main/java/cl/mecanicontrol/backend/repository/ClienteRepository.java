package cl.mecanicontrol.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByUsuarioId(UUID usuarioId);

    @Query("SELECT c FROM Cliente c WHERE c.usuario.email = :email")
    Optional<Cliente> findByUsuarioEmail(@Param("email") String email);
}
