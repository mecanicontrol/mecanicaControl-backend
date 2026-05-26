package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orden_trabajo")
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamiento_id")
    private Agendamiento agendamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_ot_id", nullable = false)
    private EstadoOt estadoOt;

    @Column(name = "codigo_ot", nullable = false, unique = true)
    private String codigoOt;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "trabajo_realizado", columnDefinition = "TEXT")
    private String trabajoRealizado;

    @Column(name = "costo_mano_obra", nullable = false)
    private BigDecimal costoManoObra;

    @Column(name = "costo_repuestos", nullable = false)
    private BigDecimal costoRepuestos;

    @Column(name = "total", insertable = false, updatable = false)
    private BigDecimal total;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "aprobada_at")
    private LocalDateTime aprobadaAt;
}
