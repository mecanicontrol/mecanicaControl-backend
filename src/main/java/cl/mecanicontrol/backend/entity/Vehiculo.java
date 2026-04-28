package cl.mecanicontrol.backend.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vehiculo")
@Getter
@Setter
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "marca_vehiculo_id")
    private UUID marcaVehiculoId;

    @Column(name = "modelo_vehiculo_id")
    private UUID modeloVehiculoId;

    @Column(name = "tipo_combustible_id")
    private UUID tipoCombustibleId;

    @Column(name = "patente")
    private String patente;

    @Column(name = "anio")
    private Short anio;

    @Column(name = "color")
    private String color;

    @Column(name = "kilometraje_ingreso")
    private Integer kilometrajeIngreso;

    @Column(name = "numero_motor")
    private String numeroMotor;

    @Column(name = "numero_chasis")
    private String numeroChasis;
}
