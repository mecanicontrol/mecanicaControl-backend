package cl.mecanicontrol.backend.repository;

import cl.mecanicontrol.backend.entity.PedidoTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoTiendaRepository extends JpaRepository<PedidoTienda, UUID> {

    List<PedidoTienda> findAllByOrderByFechaDesc();

    List<PedidoTienda> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM PedidoTienda p WHERE p.fecha >= :desde AND p.fecha < :hasta")
    BigDecimal sumTotalByFechaBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByFechaBetween(LocalDateTime desde, LocalDateTime hasta);
}