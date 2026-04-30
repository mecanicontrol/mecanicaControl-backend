package cl.mecanicontrol.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tipo_movimiento")
@Getter
@Setter
public class TipoMovimiento {

    @Id
    @Column(name = "id",  nullable = false)
    private UUID id;

    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;

    @Column(name = "signo", nullable = false)
    private Short signo;

    @Column(name = "descripcion")
    private String descripcion;
}
