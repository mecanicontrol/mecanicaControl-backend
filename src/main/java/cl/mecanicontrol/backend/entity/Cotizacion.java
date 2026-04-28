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
@Table(name = "cotizacion")
@Getter
@Setter
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private ServicioCatalogo servicio;

    @ManyToOne
    @JoinColumn(name = "marca_vehiculo_id")
    private MarcaVehiculo marcaVehiculo;

    @ManyToOne
    @JoinColumn(name = "modelo_vehiculo_id")
    private ModeloVehiculo modeloVehiculo;

    @Column(name = "anio")
    private Short anio;

    @Column(name = "patente")
    private String patente;

    @Column(name = "precio_estimado")
    private BigDecimal precioEstimado;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "expira_at")
    private LocalDateTime expiraAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
