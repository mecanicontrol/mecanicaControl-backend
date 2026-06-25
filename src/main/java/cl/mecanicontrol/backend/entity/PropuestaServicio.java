package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "propuesta_servicio")
@Getter
@Setter
public class PropuestaServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propuesta_id", nullable = false)
    private PropuestaDiagnostico propuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServicioCatalogo servicio;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "imagenes", nullable = false)
    private String imagenes = "[]";

    @Column(name = "incluido_en_original", nullable = false)
    private Boolean incluidoEnOriginal = false;

    @Column(name = "aceptado_por_cliente")
    private Boolean aceptadoPorCliente;
}