package cl.mecanicontrol.backend.entity;

import java.math.BigDecimal;
import java.util.UUID;

import cl.mecanicontrol.backend.entity.CategoriaServicio;
import cl.mecanicontrol.backend.entity.Especialidad;
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
@Table(name = "servicio_catalogo")
@Getter
@Setter

public class ServicioCatalogo {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "categoria_servicio_id", nullable=false)
    private CategoriaServicio categoriaServicio;

    @ManyToOne
    @JoinColumn(name = "especialidad_id")
    private Especialidad especialidad;
    
    @Column(name = "nombre")
    private String nombreServicio;
    @Column(name = "descripcion")
    private String descripcionServicio;
    @Column(name = "duracion_minutos")
    private Integer duracion;
    @Column(name = "precio_base")
    private BigDecimal precioBase;
    @Column(name = "activo")
    private Boolean servicioActivo;

}
