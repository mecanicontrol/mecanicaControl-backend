package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "configuracion")
@Getter
@Setter
public class Configuracion {

    @Id
    @Column(name = "clave", length = 100)
    private String clave;

    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    private String valor;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}