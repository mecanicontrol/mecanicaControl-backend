package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {

    @Query("""
        SELECT DISTINCT ot FROM OrdenTrabajo ot
        LEFT JOIN FETCH ot.agendamiento a
        LEFT JOIN FETCH a.idVehiculo
        LEFT JOIN FETCH a.servicio
        LEFT JOIN FETCH ot.tecnico t
        LEFT JOIN FETCH t.idUsuario
        LEFT JOIN FETCH ot.estadoOt
        """)
    List<OrdenTrabajo> findAllEager();

    @Query("""
        SELECT DISTINCT ot FROM OrdenTrabajo ot
        LEFT JOIN FETCH ot.agendamiento a
        LEFT JOIN FETCH a.idVehiculo
        LEFT JOIN FETCH a.servicio
        LEFT JOIN FETCH ot.tecnico t
        LEFT JOIN FETCH t.idUsuario
        LEFT JOIN FETCH ot.estadoOt
        WHERE ot.estadoOt.nombre = :nombre
        """)
    List<OrdenTrabajo> findByEstadoOtNombreEager(@Param("nombre") String nombre);

    @Query("""
        SELECT DISTINCT ot FROM OrdenTrabajo ot
        LEFT JOIN FETCH ot.agendamiento a
        LEFT JOIN FETCH a.idVehiculo
        LEFT JOIN FETCH a.servicio
        LEFT JOIN FETCH ot.tecnico t
        LEFT JOIN FETCH t.idUsuario
        LEFT JOIN FETCH ot.estadoOt
        WHERE t.idTecnico = :tecnicoId
        """)
    List<OrdenTrabajo> findByTecnicoIdEager(@Param("tecnicoId") UUID tecnicoId);

    @Query("""
        SELECT DISTINCT ot FROM OrdenTrabajo ot
        LEFT JOIN FETCH ot.agendamiento a
        LEFT JOIN FETCH a.idVehiculo
        LEFT JOIN FETCH a.servicio
        LEFT JOIN FETCH ot.tecnico t
        LEFT JOIN FETCH t.idUsuario
        LEFT JOIN FETCH ot.estadoOt
        WHERE ot.codigoOt = :codigoOt
        """)
    Optional<OrdenTrabajo> findByCodigoOtEager(@Param("codigoOt") String codigoOt);

    Optional<OrdenTrabajo> findByCodigoOt(String codigoOt);

    List<OrdenTrabajo> findByTecnicoIdTecnico(UUID tecnicoId);

    List<OrdenTrabajo> findByEstadoOtNombre(String nombre);

    Optional<OrdenTrabajo> findByAgendamientoIdAgendamiento(UUID agendamientoId);

    List<OrdenTrabajo> findByFechaInicioBetween(LocalDateTime desde, LocalDateTime hasta);
}