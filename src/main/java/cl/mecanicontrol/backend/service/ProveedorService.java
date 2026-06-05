package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.Proveedor;
import cl.mecanicontrol.backend.repository.CondicionPagoRepository;
import cl.mecanicontrol.backend.repository.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final CondicionPagoRepository condicionPagoRepository;

    public ProveedorService(ProveedorRepository proveedorRepository, CondicionPagoRepository condicionPagoRepository) {
        this.proveedorRepository = proveedorRepository;
        this.condicionPagoRepository = condicionPagoRepository;
    }

    public List<Proveedor> findAll(){
        return proveedorRepository.findAll();
    }

    public Proveedor findById(UUID id){
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    @Transactional
    public Proveedor crear(Proveedor proveedor){
        proveedor.setActivoProveedor(true);
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public Proveedor update(UUID id, Proveedor dto){
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        proveedor.setNombreProveedor(dto.getNombreProveedor());
        proveedor.setRutProveedor(dto.getRutProveedor());
        proveedor.setContactoProveedor(dto.getContactoProveedor());
        proveedor.setEmailProveedor(dto.getEmailProveedor());
        proveedor.setTelefonoProveedor(dto.getTelefonoProveedor());

        return proveedorRepository.save(proveedor);
    }

    public void toggleActivo(UUID id){
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
        proveedor.setActivoProveedor(!proveedor.getActivoProveedor());
        proveedorRepository.save(proveedor);
    }
}
