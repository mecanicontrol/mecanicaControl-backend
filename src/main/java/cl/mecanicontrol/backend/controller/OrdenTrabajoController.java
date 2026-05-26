package cl.mecanicontrol.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.mecanicontrol.backend.dto.OrdenTrabajoListDTO;
import cl.mecanicontrol.backend.dto.SeguimientoResponseDTO;
import cl.mecanicontrol.backend.entity.Agendamiento;
import cl.mecanicontrol.backend.entity.Cliente;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.Vehiculo;
import cl.mecanicontrol.backend.repository.ClienteRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.service.SeguimientoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ot")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoRepository otRepo;
    private final ClienteRepository clienteRepo;
    private final SeguimientoService seguimientoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrdenTrabajoListDTO>> findAll() {
        List<OrdenTrabajoListDTO> lista = otRepo.findAll().stream().map(ot -> {
            String vehiculoPatente = null;
            String nombreCliente   = null;
            String nombreServicio  = null;

            Agendamiento ag = ot.getAgendamiento();
            if (ag != null) {
                if (ag.getServicio() != null) nombreServicio = ag.getServicio().getNombreServicio();
                Vehiculo v = ag.getIdVehiculo();
                if (v != null) {
                    vehiculoPatente = v.getPatente();
                    if (v.getClienteId() != null) {
                        Cliente c = clienteRepo.findById(v.getClienteId()).orElse(null);
                        if (c != null && c.getUsuario() != null) {
                            nombreCliente = c.getUsuario().getNombre() + " " + c.getUsuario().getApellido();
                        }
                    }
                }
            }

            String nombreTecnico = null;
            if (ot.getTecnico() != null && ot.getTecnico().getIdUsuario() != null) {
                nombreTecnico = ot.getTecnico().getIdUsuario().getNombre()
                              + " " + ot.getTecnico().getIdUsuario().getApellido();
            }

            return new OrdenTrabajoListDTO(
                ot.getId(),
                ot.getCodigoOt(),
                ot.getEstadoOt() != null ? ot.getEstadoOt().getNombre() : null,
                nombreCliente,
                vehiculoPatente,
                nombreTecnico,
                nombreServicio,
                ot.getFechaInicio(),
                ot.getFechaCierre(),
                ot.getTotal()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeguimientoResponseDTO> findByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(seguimientoService.buscarPorCodigo(codigo));
    }
}