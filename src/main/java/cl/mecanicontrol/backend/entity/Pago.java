package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pago")
@Getter
@Setter
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",  updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id",  nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_pago_id", nullable = false)
    private EstadoPago estadoPago;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "monto_pagado", nullable = false)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "referencia_externa")
    private String referenciaExterna;

    @Column(name = "comprobante_url")
    private String comprobanteUrl;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) fechaPago = LocalDateTime.now();
        if (montoPagado == null) montoPagado = BigDecimal.ZERO;
    }
}
