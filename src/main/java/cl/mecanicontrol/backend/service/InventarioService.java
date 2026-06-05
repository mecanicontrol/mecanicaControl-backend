package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.dto.inventario.MovimientoRequestDTO;
import cl.mecanicontrol.backend.dto.inventario.ProductoRequestDTO;
import cl.mecanicontrol.backend.dto.inventario.ProductoResponseDTO;
import cl.mecanicontrol.backend.entity.MovimientoInventario;
import cl.mecanicontrol.backend.entity.Productos;
import cl.mecanicontrol.backend.entity.TipoMovimiento;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProveedorRepository proveedorRepository;
    private final CategoriaProductoRepository categoriaProductoRepository;
    private final MarcaProductoRepository marcaProductoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public InventarioService(
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            ProveedorRepository proveedorRepository,
            CategoriaProductoRepository categoriaProductoRepository,
            MarcaProductoRepository marcaProductoRepository,
            TipoMovimientoRepository tipoMovimientoRepository,
            UsuarioRepository usuarioRepository){
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.proveedorRepository = proveedorRepository;
        this.categoriaProductoRepository = categoriaProductoRepository;
        this.marcaProductoRepository = marcaProductoRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ProductoResponseDTO> findAll(){
        return productoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProductoResponseDTO> findAlertas(){
        return productoRepository.findByStockActualLessThanEqualStockMinimo()
                .stream().map(this::toDTO)
                .toList();
    }

    public ProductoResponseDTO findById(UUID id){
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return toDTO(producto);
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto){
        Productos producto = new Productos();
        producto.setSku(dto.codigoSku());
        producto.setNombreProducto(dto.nombre());
        producto.setPrecioCosto(dto.precioCosto());
        producto.setPrecioVenta(dto.precioVenta());
        producto.setStockMinimo(dto.stockMinimo());
        producto.setStockActual(0);
        producto.setUbicacionBodega(dto.ubicacionBodega());
        producto.setProductoActivo(true);

        if (dto.proveedorId() != null){
            producto.setProveedor(proveedorRepository.findById(dto.proveedorId())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado")));
        }
        if (dto.categoriaProductoId() != null){
            producto.setCategoriaProducto(categoriaProductoRepository.findById(dto.categoriaProductoId())
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada")));
        }
        if (dto.marcaProductoId() != null){
            producto.setMarcaProducto(marcaProductoRepository.findById(dto.marcaProductoId())
                    .orElseThrow(() -> new RuntimeException("Marca no encontrada")));
        }

        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponseDTO update(UUID id, ProductoRequestDTO dto){
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setNombreProducto(dto.nombre());
        producto.setPrecioCosto(dto.precioCosto());
        producto.setPrecioVenta(dto.precioVenta());
        producto.setStockMinimo(dto.stockMinimo());
        producto.setUbicacionBodega(dto.ubicacionBodega());

        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public void registrarMovimiento(MovimientoRequestDTO dto, UUID usuarioId){
        Productos producto = productoRepository.findById(dto.productoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        TipoMovimiento tipo = tipoMovimientoRepository.findByNombre(dto.tipoMovimientoNombre())
                .orElseThrow(() -> new RuntimeException("Tipo de movimiento no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProductos(producto);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setUsuario(usuario);
        movimiento.setCantidad(dto.cantidad());
        movimiento.setPrecioUnitario(dto.precioUnitario());
        movimiento.setMotivo(dto.motivo());
        movimiento.setCreatedAt(LocalDateTime.now());

        movimientoInventarioRepository.save(movimiento);
    }

    public List<MovimientoInventario> getHistorialProducto(UUID productoId){
        return movimientoInventarioRepository.findByProductosIdProducto(productoId);
    }

    private ProductoResponseDTO toDTO(Productos p){
        boolean alerta = p.getStockActual() != null && p.getStockMinimo() != null && p.getStockActual() <= p.getStockMinimo();

        return new ProductoResponseDTO(
                p.getIdProducto(),
                p.getSku(),
                p.getNombreProducto(),
                p.getCategoriaProducto() != null ? p.getCategoriaProducto().getNombre() : null,
                p.getMarcaProducto() != null ? p.getMarcaProducto().getNombre() : null,
                p.getPrecioCosto(),
                p.getPrecioVenta(),
                p.getStockActual() != null ? p.getStockActual() : 0,
                p.getStockMinimo() != null ? p.getStockMinimo() : 0,
                alerta
        );
    }
}
