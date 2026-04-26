package cl.mecanicontrol.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "modelo_vehiculo")
public class ModeloVehiculo {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marca_vehiculo_id", nullable = false)
    private MarcaVehiculo marcaVehiculo;

    @Column(name = "nombre", nullable = false)
    private String nombre;
}
