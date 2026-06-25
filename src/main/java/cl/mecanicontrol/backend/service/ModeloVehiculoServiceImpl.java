package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ModeloVehiculoServiceImpl implements ModeloVehiculoService {

    private final ModeloVehiculoRepository modeloVehiculoRepository;
    private final MarcaVehiculoRepository  marcaVehiculoRepository;

    public ModeloVehiculoServiceImpl(ModeloVehiculoRepository modeloVehiculoRepository,
                                     MarcaVehiculoRepository marcaVehiculoRepository) {
        this.modeloVehiculoRepository = modeloVehiculoRepository;
        this.marcaVehiculoRepository  = marcaVehiculoRepository;
    }

    @Override
    public List<ModeloVehiculo> findAll() {
        return modeloVehiculoRepository.findAll();
    }

    @Override
    public List<ModeloVehiculo> findByMarcaId(UUID idMarca) {
        return modeloVehiculoRepository.findByMarcaId(idMarca);
    }

    @Override
    public ModeloVehiculo saveModelo(ModeloVehiculo modeloVehiculo) {
        return modeloVehiculoRepository.save(modeloVehiculo);
    }

    @Override
    public ModeloVehiculo updateModelo(UUID id, String nombre, UUID marcaId) {
        ModeloVehiculo modelo = modeloVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado"));
        modelo.setNombre(nombre);
        if (marcaId != null) {
            MarcaVehiculo marca = marcaVehiculoRepository.findById(marcaId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca no encontrada"));
            modelo.setMarca(marca);
        }
        return modeloVehiculoRepository.save(modelo);
    }

    @Override
    public void deleteModelo(UUID id) {
        if (!modeloVehiculoRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado");
        modeloVehiculoRepository.deleteById(id);
    }
}