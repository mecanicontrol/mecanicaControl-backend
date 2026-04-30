package cl.mecanicontrol.backend.entity;

import java.math.BigDecimal;
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
@Getter
@Setter
@Table(name = "tecnico")
public class Tecnico {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID idTecnico;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario idUsuario;

    @ManyToOne
    @JoinColumn(name = "nivel_tecnico_id")
    private NivelTecnico idNivelTecnico;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name= "horas_semana_max")
    private Integer horasSemanales;

    @Column(name = "tarifa_hora")
    private BigDecimal valorHora;


}
