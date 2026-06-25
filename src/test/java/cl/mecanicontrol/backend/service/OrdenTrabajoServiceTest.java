package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.ot.AvanzarFaseDTO;
import cl.mecanicontrol.backend.dto.ot.OTRequestDTO;
import cl.mecanicontrol.backend.dto.ot.OTResponseDTO;
import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenTrabajoServiceTest {

    @Mock OrdenTrabajoRepository otRepository;
    @Mock FaseVehiculoRepository faseVehiculoRepository;
    @Mock FaseRepository faseRepository;
    @Mock TecnicoRepository tecnicoRepository;
    @Mock AgendamientoRepository agendamientoRepository;
    @Mock EstadoOtRepository estadoOtRepository;
    @Mock ClienteRepository clienteRepository;

    @InjectMocks OrdenTrabajoService otService;

    private UUID agendamientoId;
    private Agendamiento agendamiento;
    private EstadoOt estadoActiva;
    private ServicioCatalogo servicio;
    private Vehiculo vehiculo;

    @BeforeEach
    void setUp() {
        agendamientoId = UUID.randomUUID();

        servicio = new ServicioCatalogo();
        servicio.setNombreServicio("Cambio de aceite");
        servicio.setPrecioBase(BigDecimal.valueOf(25000));

        vehiculo = new Vehiculo();
        vehiculo.setPatente("AB1234");
        vehiculo.setClienteId(null); // evita consulta a clienteRepo

        agendamiento = new Agendamiento();
        agendamiento.setIdAgendamiento(agendamientoId);
        agendamiento.setIdVehiculo(vehiculo);
        agendamiento.setServicio(servicio);
        agendamiento.setServicios(List.of(servicio));
        agendamiento.setFechaInicio(LocalDateTime.now());

        estadoActiva = new EstadoOt();
        estadoActiva.setNombre("ACTIVA");
    }

    // ── TU-OT-01 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-OT-01: crear OT desde agendamiento genera código con formato OT-YYYY-NNNN")
    void crearOT_desdeAgendamientoConfirmado_generaCodigoAutomatico() {
        // Arrange
        when(agendamientoRepository.findById(agendamientoId)).thenReturn(Optional.of(agendamiento));
        when(estadoOtRepository.findByNombre("ACTIVA")).thenReturn(Optional.of(estadoActiva));
        when(otRepository.count()).thenReturn(4L); // siguiente será 5
        when(faseRepository.findAll()).thenReturn(List.of());

        // El save debe devolver una OT con todos los campos necesarios para toDTO()
        when(otRepository.save(any(OrdenTrabajo.class))).thenAnswer(inv -> {
            OrdenTrabajo ot = inv.getArgument(0);
            ot.setEstadoOt(estadoActiva);
            return ot;
        });

        OTRequestDTO request = new OTRequestDTO(agendamientoId, null);

        // Act
        OTResponseDTO result = otService.crear(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.codigoOt()).matches("OT-\\d{4}-\\d{4}");
        int anioActual = java.time.LocalDate.now().getYear();
        assertThat(result.codigoOt()).startsWith("OT-" + anioActual + "-");
        assertThat(result.codigoOt()).endsWith("0005");
    }

    // ── TU-OT-02 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-OT-02: avanzar fase con checklistJson nulo lanza RuntimeException")
    void avanzarFase_conChecklistNulo_lanzaExcepcion() {
        // Arrange
        UUID otId = UUID.randomUUID();
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setEstadoOt(estadoActiva);
        ot.setAgendamiento(agendamiento);

        Fase faseRecepcion = new Fase();
        faseRecepcion.setNombre("RECEPCION");
        faseRecepcion.setOrden((short) 1);

        FaseVehiculo fv = new FaseVehiculo();
        fv.setFase(faseRecepcion);
        fv.setInicioAt(LocalDateTime.now());
        fv.setFinAt(null); // activa

        when(otRepository.findById(otId)).thenReturn(Optional.of(ot));
        when(faseVehiculoRepository.findByOrdenTrabajoId(otId)).thenReturn(List.of(fv));

        // checklist nulo → debe lanzar excepción
        AvanzarFaseDTO dtoSinChecklist = new AvanzarFaseDTO(null, null, "observación");

        // Act + Assert
        assertThrows(RuntimeException.class,
            () -> otService.avanzarFase(otId, dtoSinChecklist));

        verify(faseVehiculoRepository, never()).save(any());
    }

    // ── TU-OT-03 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-OT-03: avanzarFase siempre completa la fase de menor orden pendiente")
    void avanzarFase_siempreCompletaPrimera_noSaltaFases() {
        // Arrange — dos fases, la de orden 1 está pendiente, la de orden 2 también
        UUID otId = UUID.randomUUID();
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setEstadoOt(estadoActiva);
        ot.setAgendamiento(agendamiento);

        Fase faseRecepcion = new Fase();
        faseRecepcion.setNombre("RECEPCION");
        faseRecepcion.setOrden((short) 1);

        Fase faseDiagnostico = new Fase();
        faseDiagnostico.setNombre("DIAGNOSTICO");
        faseDiagnostico.setOrden((short) 2);

        FaseVehiculo fvRecepcion = new FaseVehiculo();
        fvRecepcion.setFase(faseRecepcion);
        fvRecepcion.setInicioAt(LocalDateTime.now().minusHours(2));
        fvRecepcion.setFinAt(null); // pendiente

        FaseVehiculo fvDiagnostico = new FaseVehiculo();
        fvDiagnostico.setFase(faseDiagnostico);
        fvDiagnostico.setInicioAt(LocalDateTime.now().minusHours(1));
        fvDiagnostico.setFinAt(null); // pendiente

        when(otRepository.findById(otId)).thenReturn(Optional.of(ot));
        when(faseVehiculoRepository.findByOrdenTrabajoId(otId)).thenReturn(
            List.of(fvDiagnostico, fvRecepcion) // desordenadas a propósito
        );
        when(faseVehiculoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AvanzarFaseDTO dto = new AvanzarFaseDTO(null, "{}", "");

        // Act
        otService.avanzarFase(otId, dto);

        // Assert — la fase guardada debe ser RECEPCION (orden 1), no DIAGNOSTICO
        verify(faseVehiculoRepository).save(argThat(fv ->
            "RECEPCION".equals(fv.getFase().getNombre()) && fv.getFinAt() != null
        ));
    }
}
