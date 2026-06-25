package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.AgendamientoRequestDTO;
import cl.mecanicontrol.backend.dto.AgendamientoResponsedDTO;
import cl.mecanicontrol.backend.dto.DisponibilidadSlotDTO;
import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.entity.EstadoAgendamiento;
import cl.mecanicontrol.backend.entity.ServicioCatalogo;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.AgendamientoRepository;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.EstadoAgendamientoRepository;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import cl.mecanicontrol.backend.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamientoServiceTest {

    @Mock AgendamientoRepository agendamientoRepo;
    @Mock VehiculoRepository vehiculoRepo;
    @Mock ServicioCatalogoRepository servicioRepo;
    @Mock EstadoAgendamientoRepository estadoRepo;
    @Mock TecnicoRepository tecnicoRepo;
    @Mock ClienteRepository clienteRepo;

    @InjectMocks
    AgendamientoServiceImpl agendamientoService;

    private UUID vehiculoId;
    private UUID servicioId;
    private Vehiculo vehiculo;
    private ServicioCatalogo servicio;
    private EstadoAgendamiento estadoPendiente;
    private EstadoAgendamiento estadoCancelado;

    @BeforeEach
    void setUp() {
        vehiculoId = UUID.randomUUID();
        servicioId = UUID.randomUUID();

        vehiculo = new Vehiculo();
        vehiculo.setPatente("AB1234");

        servicio = new ServicioCatalogo();
        servicio.setNombreServicio("Cambio de aceite");
        servicio.setDuracion(60);

        estadoPendiente = new EstadoAgendamiento();
        estadoPendiente.setNombre("PENDIENTE");

        estadoCancelado = new EstadoAgendamiento();
        estadoCancelado.setNombre("CANCELADO");
    }

    // ── TU-AGE-01 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AGE-01: crear agendamiento con slot disponible guarda correctamente")
    void crearAgendamiento_slotDisponible_creaExitosamente() {
        // Arrange
        AgendamientoRequestDTO request = new AgendamientoRequestDTO(
            vehiculoId,
            List.of(servicioId),
            LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
            "Sin notas",
            null
        );

        Agendamiento agendamientoGuardado = new Agendamiento();
        agendamientoGuardado.setIdVehiculo(vehiculo);
        agendamientoGuardado.setServicio(servicio);
        agendamientoGuardado.setIdEstadoAgendamiento(estadoPendiente);

        when(vehiculoRepo.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(servicioRepo.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(estadoRepo.findByNombre("PENDIENTE")).thenReturn(Optional.of(estadoPendiente));
        when(agendamientoRepo.save(any(Agendamiento.class))).thenReturn(agendamientoGuardado);

        // Act
        AgendamientoResponsedDTO result = agendamientoService.crear(request);

        // Assert
        assertThat(result).isNotNull();
        verify(agendamientoRepo, times(1)).save(any(Agendamiento.class));
    }

    // ── TU-AGE-02 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AGE-02: getDisponibilidad con taller lleno retorna slots no disponibles")
    void getDisponibilidad_tallerLleno_retornaSlotNoDisponible() {
        // Arrange — simula que ya hay 15 vehículos en el taller a cualquier hora
        when(servicioRepo.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(agendamientoRepo.countVehiculosEnTallerEnSlot(any(LocalDateTime.class)))
            .thenReturn(15L); // igual al límite CAPACIDAD_MAXIMA_DIA

        // Act
        List<DisponibilidadSlotDTO> slots = agendamientoService.getDisponibilidad(
            LocalDate.now().plusDays(1), servicioId
        );

        // Assert — ningún slot debe estar disponible cuando el taller está lleno
        assertThat(slots).isNotEmpty();
        assertThat(slots).allMatch(s -> !s.disponible());
    }

    // ── TU-AGE-03 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-AGE-03: cancelar agendamiento en estado PENDIENTE cambia a CANCELADO")
    void cancelarAgendamiento_estadoPendiente_cambiaACancelado() {
        // Arrange
        UUID agendamientoId = UUID.randomUUID();

        Agendamiento agendamiento = new Agendamiento();
        agendamiento.setIdEstadoAgendamiento(estadoPendiente);
        agendamiento.setIdVehiculo(vehiculo);
        agendamiento.setServicio(servicio);

        Agendamiento agendamientoCancelado = new Agendamiento();
        agendamientoCancelado.setIdEstadoAgendamiento(estadoCancelado);
        agendamientoCancelado.setIdVehiculo(vehiculo);
        agendamientoCancelado.setServicio(servicio);

        when(agendamientoRepo.findById(agendamientoId)).thenReturn(Optional.of(agendamiento));
        when(estadoRepo.findByNombre("CANCELADO")).thenReturn(Optional.of(estadoCancelado));
        when(agendamientoRepo.save(any(Agendamiento.class))).thenReturn(agendamientoCancelado);

        // Act
        AgendamientoResponsedDTO result = agendamientoService.cancelar(agendamientoId);

        // Assert
        verify(agendamientoRepo, times(1)).save(argThat(a ->
            "CANCELADO".equals(a.getIdEstadoAgendamiento().getNombre())
        ));
        assertThat(result).isNotNull();
    }

    // ── Extra: cancelar ya cancelado lanza excepción ─────────────────────────

    @Test
    @DisplayName("cancelar agendamiento ya CANCELADO lanza ResponseStatusException")
    void cancelarAgendamiento_yaCompletado_lanzaExcepcion() {
        // Arrange
        UUID id = UUID.randomUUID();
        Agendamiento agendamiento = new Agendamiento();
        agendamiento.setIdEstadoAgendamiento(estadoCancelado);

        when(agendamientoRepo.findById(id)).thenReturn(Optional.of(agendamiento));

        // Act + Assert
        assertThrows(ResponseStatusException.class,
            () -> agendamientoService.cancelar(id));

        verify(agendamientoRepo, never()).save(any());
    }
}