package cl.mecanicontrol.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orden_trabajo")
@Getter
@Setter
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "agendamiento_id")
    private Agendamiento agendamiento;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @ManyToOne
    @JoinColumn(name = "estado_ot_id")
    private EstadoOt estadoOt;

    // Generado por trigger de BD — nunca enviar en INSERT
    @Column(name = "codigo_ot", nullable = false, unique = true, insertable = false, updatable = false)
    private String codigoOt;

    @Column(name = "diagnostico")
    private String diagnostico;

    @Column(name = "trabajo_realizado")
    private String trabajoRealizado;

    @Column(name = "costo_mano_obra")
    private BigDecimal costoManoObra;

    @Column(name = "costo_repuestos")
    private BigDecimal costoRepuestos;

    // GENERATED ALWAYS AS en BD — no insertar ni actualizar
    @Column(name = "total", insertable = false, updatable = false)
    private BigDecimal total;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "aprobada_at")
    private LocalDateTime aprobadaAt;
}