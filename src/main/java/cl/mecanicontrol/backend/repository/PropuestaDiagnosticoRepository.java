package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.PropuestaDiagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropuestaDiagnosticoRepository extends JpaRepository<PropuestaDiagnostico, UUID> {

    Optional<PropuestaDiagnostico> findByTokenCliente(String token);

    @Query("SELECT p FROM PropuestaDiagnostico p LEFT JOIN FETCH p.servicios WHERE p.ordenTrabajo.id = :otId ORDER BY p.creadoAt DESC")
    List<PropuestaDiagnostico> findByOrdenTrabajoId(UUID otId);

    @Query("SELECT p FROM PropuestaDiagnostico p LEFT JOIN FETCH p.servicios WHERE p.estado IN :estados ORDER BY p.creadoAt DESC")
    List<PropuestaDiagnostico> findByEstadoIn(List<String> estados);

    @Query("SELECT p FROM PropuestaDiagnostico p LEFT JOIN FETCH p.servicios LEFT JOIN FETCH p.ordenTrabajo WHERE p.tokenCliente = :token")
    Optional<PropuestaDiagnostico> findByTokenClienteWithOt(String token);
}