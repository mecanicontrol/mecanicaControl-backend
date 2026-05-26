package cl.mecanicontrol.backend.entity;

import java.time.LocalDateTime;
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
@Table(name = "fase_vehiculo")
@Getter
@Setter
public class FaseVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @ManyToOne
    @JoinColumn(name = "fase_id", nullable = false)
    private Fase fase;

    @Column(name = "checklist_json")
    private String checklistJson;

    @Column(name = "observaciones")
    private String observaciones;

    @Column(name = "inicio_at", nullable = false)
    private LocalDateTime inicioAt;

    @Column(name = "fin_at")
    private LocalDateTime finAt;
}