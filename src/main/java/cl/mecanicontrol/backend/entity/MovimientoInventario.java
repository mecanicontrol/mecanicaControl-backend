package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimiento_inventario")
@Getter
@Setter
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Productos productos;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "orden_trabajo_id")
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne
    @JoinColumn(name = "tipo_movimiento_id", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
