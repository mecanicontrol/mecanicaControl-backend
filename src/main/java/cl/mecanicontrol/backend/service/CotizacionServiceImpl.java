package cl.mecanicontrol.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.mecanicontrol.backend.dto.CotizacionRequestDTO;
import cl.mecanicontrol.backend.dto.CotizacionResponseDTO;
import cl.mecanicontrol.backend.entity.Cotizacion;
import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.entity.ServicioCatalogo;
import cl.mecanicontrol.backend.repository.CotizacionRepository;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import cl.mecanicontrol.backend.repository.ServicioCatalogoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotizacionServiceImpl implements CotizacionService {

    private final CotizacionRepository cotizacionRepo;
    private final ServicioCatalogoRepository servicioRepo;
    private final MarcaVehiculoRepository marcaRepo;
    private final ModeloVehiculoRepository modeloRepo;

    @Override
    public CotizacionResponseDTO crear(CotizacionRequestDTO request) {
        ServicioCatalogo servicio = servicioRepo.findById(request.servicioId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        MarcaVehiculo marca = marcaRepo.findById(request.marcaVehiculoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));

        ModeloVehiculo modelo = modeloRepo.findById(request.modeloVehiculoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado"));

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setClienteId(request.clienteId());
        cotizacion.setServicio(servicio);
        cotizacion.setMarcaVehiculo(marca);
        cotizacion.setModeloVehiculo(modelo);
        cotizacion.setAnio(request.anio());
        cotizacion.setPatente(request.patente() != null ? request.patente().toUpperCase() : null);
        cotizacion.setPrecioEstimado(servicio.getPrecioBase());
        cotizacion.setCreatedAt(LocalDateTime.now());
        cotizacion.setExpiraAt(LocalDateTime.now().plusDays(30));

        Cotizacion guardada = cotizacionRepo.save(cotizacion);
        log.info("Cotización creada: {}", guardada.getId());

        return toDTO(guardada);
    }

    @Override
    public List<CotizacionResponseDTO> findByCliente(UUID clienteId) {
        return cotizacionRepo.findByClienteId(clienteId)
            .stream().map(this::toDTO).toList();
    }

    private CotizacionResponseDTO toDTO(Cotizacion c) {
        return new CotizacionResponseDTO(
            c.getId(),
            c.getServicio() != null ? c.getServicio().getNombreServicio() : null,
            c.getServicio() != null ? c.getServicio().getDescripcionServicio() : null,
            c.getMarcaVehiculo() != null ? c.getMarcaVehiculo().getNombre() : null,
            c.getModeloVehiculo() != null ? c.getModeloVehiculo().getNombre() : null,
            c.getAnio(),
            c.getPatente(),
            c.getPrecioEstimado(),
            c.getExpiraAt(),
            c.getCreatedAt()
        );
    }
}
