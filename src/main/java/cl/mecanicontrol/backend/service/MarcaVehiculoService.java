package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;

import java.util.List;
import java.util.UUID;

public interface MarcaVehiculoService {

    MarcaVehiculo findByIdMarca(UUID idMarca);
    MarcaVehiculo createMarca(MarcaVehiculo marcaVehiculo);
    List<MarcaVehiculo> findAll();
}
