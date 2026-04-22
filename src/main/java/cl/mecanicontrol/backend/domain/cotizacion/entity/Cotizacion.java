package cl.mecanicontrol.backend.domain.cotizacion.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name="cotizacion")
public class Cotizacion {

    @Id
    private UUID id;

}
