package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;

import java.util.List;
import java.util.UUID;

public interface MarcaVehiculoService {
    List<MarcaVehiculo> findAll();
    MarcaVehiculo findByIdMarca(UUID idMarca);
    MarcaVehiculo createMarca(MarcaVehiculo marcaVehiculo);
    MarcaVehiculo updateMarca(UUID id, String nombre);
    void deleteMarca(UUID id);
}