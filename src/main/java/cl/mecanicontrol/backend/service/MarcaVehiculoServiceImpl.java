package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.MarcaVehiculo;
import cl.mecanicontrol.backend.repository.MarcaVehiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MarcaVehiculoServiceImpl implements MarcaVehiculoService {

    private final MarcaVehiculoRepository marcaVehiculoRepository;

    public MarcaVehiculoServiceImpl(MarcaVehiculoRepository marcaVehiculoRepository) {
        this.marcaVehiculoRepository = marcaVehiculoRepository;
    }

    @Override
    public List<MarcaVehiculo> findAll() {
        return marcaVehiculoRepository.findAll();
    }

    @Override
    public MarcaVehiculo findByIdMarca(UUID idMarca) {
        return marcaVehiculoRepository.findById(idMarca).orElse(null);
    }

    @Override
    public MarcaVehiculo createMarca(MarcaVehiculo marcaVehiculo) {
        return marcaVehiculoRepository.save(marcaVehiculo);
    }

    @Override
    public MarcaVehiculo updateMarca(UUID id, String nombre) {
        MarcaVehiculo marca = marcaVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
        marca.setNombre(nombre);
        return marcaVehiculoRepository.save(marca);
    }

    @Override
    public void deleteMarca(UUID id) {
        if (!marcaVehiculoRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada");
        marcaVehiculoRepository.deleteById(id);
    }
}