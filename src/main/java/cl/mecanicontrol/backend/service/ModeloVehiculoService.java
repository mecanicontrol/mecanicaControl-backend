package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.ModeloVehiculo;

import java.util.List;
import java.util.UUID;

public interface ModeloVehiculoService {
    List<ModeloVehiculo> findAll();
    List<ModeloVehiculo> findByMarcaId(UUID idMarca);
    ModeloVehiculo saveModelo(ModeloVehiculo modeloVehiculo);
}
