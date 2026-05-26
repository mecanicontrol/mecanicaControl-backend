package cl.mecanicontrol.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.mecanicontrol.backend.entity.Especialidad;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {

    @Query(value = """
        SELECT e.nombre FROM especialidad e
        JOIN tecnico_especialidad te ON te.especialidad_id = e.id
        WHERE te.tecnico_id = :tecnicoId
        ORDER BY te.es_principal DESC
        """, nativeQuery = true)
    List<String> findNombresByTecnicoId(@Param("tecnicoId") UUID tecnicoId);
}
