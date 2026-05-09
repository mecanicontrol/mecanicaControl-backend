package cl.mecanicontrol.backend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "proveedor")
@Getter
@Setter

public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private UUID idProveedor;

    @ManyToOne
    @JoinColumn(name = "condicion_pago_id")
    private CondicionPago condicionPago;

    @Column(name = "nombre")
    private String nombreProveedor;

    @Column(name = "rut")
    private String rutProveedor;

    @Column(name = "contacto")
    private String contactoProveedor;

    @Column(name = "email")
    private String emailProveedor;

    @Column(name = "telefono")
    private String telefonoProveedor;

    @Column(name = "activo")
    private Boolean activoProveedor;





}
