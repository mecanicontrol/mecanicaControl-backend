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
