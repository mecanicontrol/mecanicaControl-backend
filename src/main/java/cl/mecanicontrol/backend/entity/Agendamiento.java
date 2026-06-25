package cl.mecanicontrol.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "agendamiento")
@Getter
@Setter

public class Agendamiento {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private UUID idAgendamiento;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico idTecnico;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo idVehiculo;

    @ManyToOne
    @JoinColumn(name="servicio_id")
    private ServicioCatalogo servicio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "agendamiento_servicio",
        joinColumns = @JoinColumn(name = "agendamiento_id"),
        inverseJoinColumns = @JoinColumn(name = "servicio_id")
    )
    private List<ServicioCatalogo> servicios = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "estado_agendamiento_id")
    private EstadoAgendamiento idEstadoAgendamiento;

    @Column(name="fecha_hora_inicio")
    private LocalDateTime fechaInicio;

    @Column(name="fecha_hora_fin")
    private LocalDateTime fechaFin;
    
    @Column(name="notas_cliente")
    private String notaCliente;

    @Column(name="precio_acordado")
    private BigDecimal precioAcordado;

    @Column(name="created_at")
    private LocalDateTime createdAtAgendamiento;
}
