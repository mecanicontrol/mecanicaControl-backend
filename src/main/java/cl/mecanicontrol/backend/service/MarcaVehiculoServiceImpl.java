package cl.mecanicontrol.backend.service;


import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MarcaVehiculoServiceImpl implements MarcaVehiculoService {
    private MarcaVehiculoRepository marcaVehiculoRepository;

    @Override
    public List<MarcaVehiculo> findAll(){
        return marcaVehiculoRepository.findAll();
    }

    @Override
    public MarcaVehiculo findByIdMarca(UUID idMarca){
        return marcaVehiculoRepository.findById(idMarca).orElse(null
        );
    }

    @Override
    public MarcaVehiculo createMarca(MarcaVehiculo marcaVehiculo){
        return marcaVehiculoRepository.save(marcaVehiculo);
    }

}
