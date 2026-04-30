package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.dto.catalogo.CatalogoDTO;
import cl.mecanicontrol.backend.dto.catalogo.ModeloVehiculoDTO;
import cl.mecanicontrol.backend.service.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/roles")
    public ResponseEntity<List<CatalogoDTO>> getRoles(){
        return ResponseEntity.ok(catalogoService.getRoles());
    }

    @GetMapping("/niveles-tecnico")
    public ResponseEntity<List<CatalogoDTO>> getNivelesTecnico(){
        return ResponseEntity.ok(catalogoService.getNivelesTecnico());
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<CatalogoDTO>> getEspecialidades(){
        return ResponseEntity.ok(catalogoService.getEspecialidades());
    }

    @GetMapping("/marcas-vehiculo")
    public ResponseEntity<List<CatalogoDTO>> getMarcasVehiculo(){
        return ResponseEntity.ok(catalogoService.getMarcasVehiculo());
    }

    @GetMapping("/modelos-vehiculos")
    public ResponseEntity<List<ModeloVehiculoDTO>> getModelosByMarca(@RequestParam UUID marcaId){
        return ResponseEntity.ok(catalogoService.getModelosByMarca(marcaId));
    }

    @GetMapping("/combustibles")
    public ResponseEntity<List<CatalogoDTO>> getTiposCombustible(){
        return ResponseEntity.ok(catalogoService.getTiposCombustible());
    }

    @GetMapping("/categorias-servicio")
    public ResponseEntity<List<CatalogoDTO>> getCategoriasServicio(){
        return ResponseEntity.ok(catalogoService.getCategoriasServicio());
    }

    @GetMapping("/servicios")
    public ResponseEntity<List<CatalogoDTO>> getServiciosActivos(){
        return ResponseEntity.ok(catalogoService.getServiciosActivos());
    }

    @GetMapping("/estados-ot")
    public ResponseEntity<List<CatalogoDTO>> getEstadosOt(){
        return ResponseEntity.ok(catalogoService.getEstadosOt());
    }

    @GetMapping("/fases")
    public  ResponseEntity<List<CatalogoDTO>> getFases(){
        return ResponseEntity.ok(catalogoService.getFases());
    }

    @GetMapping("/metodos-pago")
    public ResponseEntity<List<CatalogoDTO>> getMetodosPago(){
        return ResponseEntity.ok(catalogoService.getMetodosPago());
    }

    @GetMapping("/niveles-fidelizacion")
    public ResponseEntity<List<CatalogoDTO>> getNivelesFidelizacion(){
        return ResponseEntity.ok(catalogoService.getNivelesFidelizacion());
    }
}
