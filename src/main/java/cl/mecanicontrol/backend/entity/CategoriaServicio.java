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
@Table(name = "categoria_servicio")
@Getter
@Setter

public class CategoriaServicio {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name= "id")
    private UUID id_categoria_servicio;
    @Column(name= "nombre")
    private String nombreCategoriaServicio;
    @Column(name= "descripcion")
    private String descripcionCategoriaService;




}
