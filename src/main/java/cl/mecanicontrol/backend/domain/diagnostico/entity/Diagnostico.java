package cl.mecanicontrol.backend.domain.diagnostico.entity;

import java.time.LocalDateTime;
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
@Table(name="diagnostico_ia")
@Getter
@Setter

public class Diagnostico {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private UUID id;
    private UUID cliente_id;
    @Column(name = "descripcion_fallo")
    private String fallo;
    private String marca;
    private String modelo;
    private Integer anio;
    private Integer kilometraje;
    private String respuesta_ia;
    private String servicio_jso;
    private String modelo_ia;
    private LocalDateTime created_at;

    

    

}
