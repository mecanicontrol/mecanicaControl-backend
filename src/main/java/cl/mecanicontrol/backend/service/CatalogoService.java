package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.catalogo.CatalogoDTO;
import cl.mecanicontrol.backend.dto.catalogo.ModeloVehiculoDTO;
import cl.mecanicontrol.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogoService {

    private final RolRepository rolRepository;
    private final NivelTecnicoRepository nivelTecnicoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final MarcaVehiculoRepository marcaVehiculoRepository;
    private final ModeloVehiculoRepository modeloVehiculoRepository;
    private final TipoCombustibleRepository tipoCombustibleRepository;
    private final CategoriaServicioRepository categoriaServicioRepository;
    private final ServicioCatalogoRepository servicioCatalogoRepository;
    private final EstadoOtRepository estadoOtRepository;
    private final FaseRepository faseRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final NivelFidelizacionRepository nivelFidelizacionRepository;

    public CatalogoService(RolRepository rolRepository,
                           NivelTecnicoRepository nivelTecnicoRepository,
                           EspecialidadRepository especialidadRepository,
                           MarcaVehiculoRepository marcaVehiculoRepository,
                           ModeloVehiculoRepository modeloVehiculoRepository,
                           TipoCombustibleRepository tipoCombustibleRepository,
                           CategoriaServicioRepository categoriaServicioRepository,
                           ServicioCatalogoRepository servicioCatalogoRepository,
                           EstadoOtRepository estadoOtRepository,
                           FaseRepository faseRepository,
                           MetodoPagoRepository metodoPagoRepository,
                           NivelFidelizacionRepository nivelFidelizacionRepository){
        this.rolRepository = rolRepository;
        this.nivelTecnicoRepository = nivelTecnicoRepository;
        this.especialidadRepository = especialidadRepository;
        this.marcaVehiculoRepository = marcaVehiculoRepository;
        this.modeloVehiculoRepository = modeloVehiculoRepository;
        this.tipoCombustibleRepository = tipoCombustibleRepository;
        this.categoriaServicioRepository = categoriaServicioRepository;
        this.servicioCatalogoRepository = servicioCatalogoRepository;
        this.estadoOtRepository = estadoOtRepository;
        this.faseRepository = faseRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.nivelFidelizacionRepository = nivelFidelizacionRepository;
    }

    public List<CatalogoDTO> getRoles(){
        return rolRepository.findAll().stream()
                .map(r -> new CatalogoDTO(r.getId(), r.getNombre(), r.getDescripcion()))
                .toList();
    }

    public List<CatalogoDTO> getNivelesTecnico(){
        return nivelTecnicoRepository.findAll().stream()
                .map(n -> new CatalogoDTO(n.getId(), n.getNombre(), n.getDescripcion()))
                .toList();
    }

    public List<CatalogoDTO> getEspecialidades(){
        return especialidadRepository.findAll().stream()
                .map(e -> new CatalogoDTO(e.getId(), e.getNombre(), e.getDescripcion()))
                .toList();
    }

    public List<CatalogoDTO> getMarcasVehiculo(){
        return marcaVehiculoRepository.findAll().stream()
                .map(m -> new CatalogoDTO(m.getId(), m.getNombre(), null))
                .toList();
    }

    public List<ModeloVehiculoDTO> getModelosByMarca(UUID marcaId){
        return modeloVehiculoRepository.findAll().stream()
                .filter(m -> m.getMarca().getId().equals(marcaId))
                .map(m -> new ModeloVehiculoDTO(
                        m.getId(),
                        m.getMarca().getId(),
                        m.getMarca().getNombre(),
                        m.getNombre()))
                .toList();
    }

    public List<CatalogoDTO> getTiposCombustible(){
        return tipoCombustibleRepository.findAll().stream()
                .map(t -> new CatalogoDTO(t.getId(), t.getNombre(), null))
                .toList();
    }

    public List<CatalogoDTO> getCategoriasServicio(){
       return categoriaServicioRepository.findAll().stream()
               .map(c -> new CatalogoDTO(c.getId_categoria_servicio(), c.getNombreCategoriaServicio(), c.getDescripcionCategoriaService()))
               .toList();
    }

    public List<CatalogoDTO> getServiciosActivos(){
        return servicioCatalogoRepository.findAll().stream()
                .filter(s -> s.getServicioActivo())
                .map(s -> new CatalogoDTO(s.getId(), s.getNombreServicio(), s.getDescripcionServicio()))
                .toList();
    }

    public List<CatalogoDTO> getEstadosOt(){
        return estadoOtRepository.findAll().stream()
                .map(e -> new CatalogoDTO(e.getId(), e.getNombre(), e.getDescripcion()))
                .toList();
    }

    public List<CatalogoDTO> getFases(){
        return faseRepository.findAll().stream()
                .map(f -> new CatalogoDTO(f.getId(), f.getNombre(), f.getDescripcion()))
                .toList();
    }

    public  List<CatalogoDTO> getMetodosPago(){
        return metodoPagoRepository.findAll().stream()
                .map(m -> new CatalogoDTO(m.getId(), m.getNombre(), null))
                .toList();
    }

    public List<CatalogoDTO> getNivelesFidelizacion(){
        return nivelFidelizacionRepository.findAll().stream()
                .map(n -> new CatalogoDTO(n.getId(), n.getNombre(), n.getDescripcion()))
                .toList();
    }

}
