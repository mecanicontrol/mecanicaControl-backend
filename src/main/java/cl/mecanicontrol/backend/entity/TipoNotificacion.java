package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tipo_notificacion")
@Getter
@Setter
public class TipoNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;
}
