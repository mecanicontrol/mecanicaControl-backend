package cl.mecanicontrol.backend.scheduler;

import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.entity.EstadoAgendamiento;
import cl.mecanicontrol.backend.repository.AgendamientoRepository;
import cl.mecanicontrol.backend.repository.EstadoAgendamientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AgendamientoCancelacionScheduler {

    private final AgendamientoRepository     agendamientoRepo;
    private final EstadoAgendamientoRepository estadoRepo;

    public AgendamientoCancelacionScheduler(AgendamientoRepository agendamientoRepo,
                                            EstadoAgendamientoRepository estadoRepo) {
        this.agendamientoRepo = agendamientoRepo;
        this.estadoRepo       = estadoRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cancelarVencidosAlIniciar() {
        cancelarVencidos();
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cancelarVencidos() {
        LocalDateTime ahora = LocalDateTime.now();

        EstadoAgendamiento estadoCancelado = estadoRepo.findByNombre("CANCELADO")
                .orElse(null);

        if (estadoCancelado == null) {
            log.warn("[Scheduler] No se encontró el estado CANCELADO en la BD — se omite la tarea.");
            return;
        }

        List<Agendamiento> vencidos = agendamientoRepo.findVencidosSinCompletar(ahora);

        if (vencidos.isEmpty()) {
            log.debug("[Scheduler] Sin agendamientos vencidos a las {}", ahora);
            return;
        }

        for (Agendamiento ag : vencidos) {
            ag.setIdEstadoAgendamiento(estadoCancelado);
        }
        agendamientoRepo.saveAll(vencidos);

        log.info("[Scheduler] {} agendamiento(s) cancelado(s) automáticamente por vencimiento ({})",
                vencidos.size(), ahora);
    }
}