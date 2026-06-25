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

    @Query(value = """
        SELECT
          ot.codigo_ot,
          ot.fecha_inicio,
          eot.nombre        AS estado,
          sc.nombre         AS servicio,
          v.patente,
          mv.nombre         AS marca,
          mo.nombre         AS modelo,
          u.nombre          AS cliente_nombre,
          u.apellido        AS cliente_apellido
        FROM orden_trabajo ot
        JOIN estado_ot eot        ON eot.id = ot.estado_ot_id
        LEFT JOIN agendamiento a  ON a.id  = ot.agendamiento_id
        LEFT JOIN vehiculo v      ON v.id  = a.vehiculo_id
        LEFT JOIN marca_vehiculo mv  ON mv.id = v.marca_vehiculo_id
        LEFT JOIN modelo_vehiculo mo ON mo.id = v.modelo_vehiculo_id
        LEFT JOIN cliente c       ON c.id  = v.cliente_id
        LEFT JOIN usuario u       ON u.id  = c.usuario_id
        LEFT JOIN servicio_catalogo sc ON sc.id = a.servicio_id
        WHERE ot.tecnico_id = :tecnicoId
        ORDER BY ot.fecha_inicio DESC
        """, nativeQuery = true)
    List<Object[]> findResumenByTecnicoId(@Param("tecnicoId") UUID tecnicoId);

    @Query(value = """
        SELECT
          ot.id,
          ot.codigo_ot,
          eot.nombre        AS estado,
          sc.nombre         AS servicio,
          v.patente,
          mv.nombre         AS marca,
          mo.nombre         AS modelo,
          u.nombre          AS cliente_nombre,
          u.apellido        AS cliente_apellido,
          ot.diagnostico,
          ot.fecha_inicio,
          ot.fecha_cierre,
          ot.costo_mano_obra,
          ot.costo_repuestos,
          ot.tecnico_id
        FROM orden_trabajo ot
        JOIN estado_ot eot        ON eot.id = ot.estado_ot_id
        LEFT JOIN agendamiento a  ON a.id  = ot.agendamiento_id
        LEFT JOIN vehiculo v      ON v.id  = a.vehiculo_id
        LEFT JOIN marca_vehiculo mv  ON mv.id = v.marca_vehiculo_id
        LEFT JOIN modelo_vehiculo mo ON mo.id = v.modelo_vehiculo_id
        LEFT JOIN cliente c       ON c.id  = v.cliente_id
        LEFT JOIN usuario u       ON u.id  = c.usuario_id
        LEFT JOIN servicio_catalogo sc ON sc.id = a.servicio_id
        WHERE ot.codigo_ot = :codigoOt
        """, nativeQuery = true)
    List<Object[]> findDetalleByCodigoOt(@Param("codigoOt") String codigoOt);

    // Cuenta vehículos físicamente en el taller (estados activos)
    @Query("SELECT COUNT(ot) FROM OrdenTrabajo ot WHERE ot.estadoOt.nombre IN ('EN_PROCESO','CONTROL_CALIDAD','LISTA_ENTREGA')")
    long countVehiculosEnTaller();

    // Fecha estimada de salida más próxima (para calcular próxima disponibilidad)
    @Query("SELECT MIN(ot.fechaEstimadaSalida) FROM OrdenTrabajo ot WHERE ot.estadoOt.nombre IN ('EN_PROCESO','CONTROL_CALIDAD','LISTA_ENTREGA') AND ot.fechaEstimadaSalida IS NOT NULL")
    java.time.LocalDateTime findProximaFechaSalida();

    // OTs activas de un técnico (para mostrar carga en modal de asignación)
    @Query(value = """
        SELECT ot.codigo_ot, eot.nombre AS estado, v.patente, sc.nombre AS servicio
        FROM orden_trabajo ot
        JOIN estado_ot eot ON eot.id = ot.estado_ot_id
        LEFT JOIN agendamiento a ON a.id = ot.agendamiento_id
        LEFT JOIN vehiculo v ON v.id = a.vehiculo_id
        LEFT JOIN servicio_catalogo sc ON sc.id = a.servicio_id
        WHERE ot.tecnico_id = :tecnicoId
          AND eot.nombre IN ('ACTIVA','EN_PROCESO','CONTROL_CALIDAD','LISTA_ENTREGA')
        ORDER BY ot.fecha_inicio ASC
        """, nativeQuery = true)
    List<Object[]> findOtActivasByTecnicoId(@Param("tecnicoId") UUID tecnicoId);
}