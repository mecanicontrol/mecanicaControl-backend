package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cl.mecanicontrol.backend.entity.PrediccionTiempoOT;

@Repository
public interface PrediccionTiempoRepository extends JpaRepository<PrediccionTiempoOT, UUID> {
    Optional<PrediccionTiempoOT> findByOrdenTrabajo_Id(UUID ordenTrabajoId);

    @Modifying
    @Transactional
    @Query("UPDATE PrediccionTiempoOT p SET p.tiempoRealMin = :tiempoReal WHERE p.ordenTrabajo.id = :otId")
    void updateTiempoReal(@Param("otId") UUID otId, @Param("tiempoReal") int tiempoReal);

    /** Average completed-phase durations (minutes) grouped by phase name, optionally filtered by service category */
    @Query(value = """
        SELECT f.nombre AS fase_nombre,
               AVG(EXTRACT(EPOCH FROM (fv.fin_at - fv.inicio_at)) / 60) AS avg_min
        FROM fase_vehiculo fv
        JOIN fase f ON f.id = fv.fase_id
        JOIN orden_trabajo ot ON ot.id = fv.orden_trabajo_id
        JOIN agendamiento ag ON ag.id = ot.agendamiento_id
        JOIN servicio_catalogo sc ON sc.id = ag.servicio_id
        WHERE fv.fin_at IS NOT NULL
          AND (:categoriaId IS NULL OR sc.categoria_servicio_id = CAST(:categoriaId AS uuid))
        GROUP BY f.nombre
        """, nativeQuery = true)
    List<Object[]> avgDuracionPorFase(@Param("categoriaId") String categoriaId);
}