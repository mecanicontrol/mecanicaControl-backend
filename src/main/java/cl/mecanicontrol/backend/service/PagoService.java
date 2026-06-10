package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.pago.PagoRequestDTO;
import cl.mecanicontrol.backend.dto.pago.PagoResponseDTO;
import cl.mecanicontrol.backend.entity.EstadoPago;
import cl.mecanicontrol.backend.entity.MetodoPago;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.Pago;
import cl.mecanicontrol.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final ClienteRepository clienteRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoPagoRepository estadoPagoRepository;

    public PagoService(PagoRepository pagoRepository,
                       OrdenTrabajoRepository ordenTrabajoRepository,
                       ClienteRepository clienteRepository,
                       MetodoPagoRepository metodoPagoRepository,
                       EstadoPagoRepository estadoPagoRepository) {
        this.pagoRepository = pagoRepository;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.clienteRepository = clienteRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.estadoPagoRepository = estadoPagoRepository;
    }

    public PagoResponseDTO registrar(PagoRequestDTO dto){
        OrdenTrabajo ot = ordenTrabajoRepository.findById(dto.ordenTrabajoId())
                .orElseThrow(() -> new RuntimeException("OT no encontrada"));

        MetodoPago metodo = metodoPagoRepository.findByNombre(dto.metodoPagoNombre())
                .orElseThrow(() -> new RuntimeException("Metodo de pago no encontrado"));

        EstadoPago estadoPago = estadoPagoRepository.findByNombre("COMPLETO")
                .orElseThrow(() -> new RuntimeException("Estado pago no encontrado"));

        Pago pago = new Pago();
        pago.setOrdenTrabajo(ot);
        pago.setMetodoPago(metodo);
        pago.setEstadoPago(estadoPago);
        pago.setMontoTotal(dto.montoTotal());
        pago.setMontoPagado(dto.montoPagado() != null ? dto.montoPagado() : dto.montoTotal());
        pago.setReferenciaExterna(dto.referenciaExterna());

        if(dto.clienteId() != null){
            pago.setCliente(clienteRepository.findById(dto.clienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado")));
        }

        return toDTO(pagoRepository.save(pago));
    }

    public List<PagoResponseDTO> findByOT(UUID otId){
        return pagoRepository.findByOrdenTrabajoId(otId).stream()
                .map(this :: toDTO)
                .toList();
    }

    public List<PagoResponseDTO> findByCliente(UUID clienteId){
        return pagoRepository.findByClienteId(clienteId).stream()
                .map(this::toDTO)
                .toList();
    }

    private PagoResponseDTO toDTO(Pago pago){
        String cliente = pago.getCliente() != null ?
                pago.getCliente().getUsuario().getNombre() : "N/A";

        return new PagoResponseDTO(
                pago.getId(),
                pago.getOrdenTrabajo().getCodigoOt(),
                cliente,
                pago.getMetodoPago().getNombre(),
                pago.getEstadoPago().getNombre(),
                pago.getMontoTotal(),
                pago.getMontoPagado(),
                pago.getReferenciaExterna(),
                pago.getFechaPago());
    }

}
