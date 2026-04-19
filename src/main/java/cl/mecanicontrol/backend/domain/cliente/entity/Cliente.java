package cl.mecanicontrol.backend.domain.cliente.entity;

import java.math.BigDecimal;
import java.util.UUID;

import cl.mecanicontrol.backend.domain.nivelFidelizacion.entity.NivelFidelizacion;
import cl.mecanicontrol.backend.domain.usuario.entity.Usuario;
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
@Table(name = "cliente")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
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
