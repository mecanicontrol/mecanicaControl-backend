package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "propuesta_diagnostico")
@Getter
@Setter
public class PropuestaDiagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @Column(name = "estado", nullable = false)
    private String estado = "PENDIENTE_ADMIN";

    @Column(name = "token_cliente", unique = true)
    private String tokenCliente;

    @Column(name = "nota_tecnico")
    private String notaTecnico;

    @Column(name = "nota_admin")
    private String notaAdmin;

    @Column(name = "nota_cliente")
    private String notaCliente;

    @Column(name = "acepta_disclaimer")
    private Boolean aceptaDisclaimer = false;

    @Column(name = "creado_at", nullable = false)
    private LocalDateTime creadoAt = LocalDateTime.now();

    @Column(name = "resuelto_at")
    private LocalDateTime resueltoAt;

    @OneToMany(mappedBy = "propuesta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PropuestaServicio> servicios = new ArrayList<>();
}