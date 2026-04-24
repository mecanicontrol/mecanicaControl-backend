package cl.mecanicontrol.backend.entity;

import java.math.BigDecimal;
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
@Table(name = "nivel_fidelizacion")
@Getter
@Setter
public class NivelFidelizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", unique = true)
    private String nombre;

    @Column(name = "puntos_min")
    private Integer puntosMin;

    @Column(name = "puntos_max")
    private Integer puntosMax;

    @Column(name = "descuento_pct")
    private BigDecimal descuentoPct;

    @Column(name = "descripcion")
    private String descripcion;

}
