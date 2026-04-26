package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "estado_agendamiento")
@Getter
@Setter
@NoArgsConstructor
public class EstadoAgendamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nombre",  nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;
}
