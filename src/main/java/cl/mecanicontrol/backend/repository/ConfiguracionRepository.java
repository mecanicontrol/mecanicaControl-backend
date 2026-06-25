package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
}