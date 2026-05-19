package cl.mecanicontrol.backend.entity;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",  updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "nivel_fidelizacion_id", nullable = false)
    private NivelFidelizacion nivelFidelizacion;

    @Column(name = "empresa")
    private String empresa;

    @Column(name = "descuento_default")
    private BigDecimal descuentoDefault;

    @Column(name = "puntos_fidelizacion")
    private Integer puntosFidelizacion;

}
