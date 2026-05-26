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
@Table(name = "prediccion_tiempo_ot")
@Getter
@Setter
public class PrediccionTiempoOT {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @Column(name = "tiempo_estimado_min", nullable = false)
    private Integer tiempoEstimadoMin;

    @Column(name = "hora_inicio_estimada")
    private LocalDateTime horaInicioEstimada;

    @Column(name = "hora_fin_estimada")
    private LocalDateTime horaFinEstimada;

    // JSON: {"RECEPCION":10,"DIAGNOSTICO":25,"EN_TRABAJO":70,"CONTROL_CALIDAD":10,"LISTO_ENTREGA":5}
    @Column(name = "desglose_fases_json")
    private String desgloseFasesJson;

    @Column(name = "confianza_pct")
    private BigDecimal confianzaPct;

    @Column(name = "factores_json")
    private String factoresJson;

    @Column(name = "tiempo_real_min")
    private Integer tiempoRealMin;

    @Column(name = "modelo_ia")
    private String modeloIa;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}