package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.Productos;
import cl.mecanicontrol.backend.repository.ProductosRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Lógica de negocio de inventario: validaciones de stock y alertas.
 * La actualización física del stock la realiza el trigger de BD.
 */
@Service
public class InventarioService {

    private final ProductosRepository productosRepo;

    public InventarioService(ProductosRepository productosRepo) {
        this.productosRepo = productosRepo;
    }

    /**
     * Valida que haya stock suficiente antes de registrar una salida.
     * Lanza excepción si stockActual < cantidad.
     */
    public void validarSalidaStock(Productos producto, int cantidad) {
        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
        if (stockActual < cantidad) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Stock insuficiente. Stock actual: " + stockActual + ", solicitado: " + cantidad
            );
        }
    }

    /**
     * Retorna todos los productos cuyo stockActual <= stockMinimo (alertas críticas).
     */
    public List<Productos> obtenerProductosBajoMinimo() {
        return productosRepo.findAll().stream()
            .filter(p ->
                Boolean.TRUE.equals(p.getProductoActivo()) &&
                p.getStockActual() != null &&
                p.getStockMinimo() != null &&
                p.getStockActual() <= p.getStockMinimo()
            )
            .toList();
    }
}
