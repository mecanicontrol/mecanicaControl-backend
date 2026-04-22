package cl.mecanicontrol.backend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "perfil_usuario")
@Getter
@Setter
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "rut")
    private String rut;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "preferencias", columnDefinition = "jsonb")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String preferencias = "{}";

    @Column(name = "ultima_sesion")
    private LocalDateTime ultimaSesion;

    @PrePersist
    protected void onCreate() {
        if(preferencias == null) {
            preferencias = "{}";
        }
    }
}
