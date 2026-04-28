package cl.mecanicontrol.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cl.mecanicontrol.backend.entity.Agendamiento;

@Repository
public interface AgendamientoRepository extends JpaRepository<Agendamiento, UUID> {

    @Query("SELECT a FROM Agendamiento a WHERE a.idVehiculo.clienteId = :clienteId")
    List<Agendamiento> findByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT a FROM Agendamiento a WHERE a.idTecnico.idTecnico = :tecnicoId")
    List<Agendamiento> findByTecnicoId(@Param("tecnicoId") UUID tecnicoId);

    @Query("SELECT a FROM Agendamiento a WHERE a.idEstadoAgendamiento.nombre = :nombre")
    List<Agendamiento> findByEstadoNombre(@Param("nombre") String nombre);

    @Query("SELECT a FROM Agendamiento a WHERE (:estado IS NULL OR a.idEstadoAgendamiento.nombre = :estado) AND (:desde IS NULL OR a.fechaInicio >= :desde) AND (:hasta IS NULL OR a.fechaInicio < :hasta)")
    List<Agendamiento> findAllFiltrado(@Param("estado") String estado, @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Agendamiento a WHERE a.idTecnico.idTecnico = :tecnicoId AND a.fechaInicio < :fin AND a.fechaFin > :inicio AND a.idEstadoAgendamiento.nombre != 'CANCELADO'")
    boolean existeConflictoHorario(@Param("tecnicoId") UUID tecnicoId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT a FROM Agendamiento a WHERE a.idEstadoAgendamiento.nombre = 'CONFIRMADO' AND a.fechaInicio >= :desde AND a.fechaInicio < :hasta")
    List<Agendamiento> findConfirmadosEnFecha(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
