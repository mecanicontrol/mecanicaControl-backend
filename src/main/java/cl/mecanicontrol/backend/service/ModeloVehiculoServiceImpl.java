package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.ModeloVehiculo;
import cl.mecanicontrol.backend.repository.ModeloVehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModeloVehiculoServiceImpl implements ModeloVehiculoService{
    private final ModeloVehiculoRepository modeloVehiculoRepository;

    @Override
    public List<ModeloVehiculo> findAll(){
        return modeloVehiculoRepository.findAll();
    }

    @Override
    public List<ModeloVehiculo> findByMarcaId(UUID idMarca){
        return modeloVehiculoRepository.findByMarcaId(idMarca);
    }

    @Override
    public ModeloVehiculo saveModelo(ModeloVehiculo modeloVehiculo){
        return modeloVehiculoRepository.save(modeloVehiculo);
    }
}