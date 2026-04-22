package cl.mecanicontrol.backend.domain.diagnostico.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import cl.mecanicontrol.backend.domain.cliente.entity.Cliente;
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
@Table(name="diagnostico_ia")
@Getter
@Setter

public class Diagnostico {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    @Column(name = "descripcion_fallo")
    private String fallo;
    private String marca;
    private String modelo;
    private Short anio;
    private Integer kilometraje;

    @Column(name = "respuesta_ia")
    private String respuestaIa;
    @Column(name = "servicios_json")
    private String servicioJso;
    @Column(name = "modelo_ia")
    private String modeloIa;
    private LocalDateTime createdAt;

    

    

}
