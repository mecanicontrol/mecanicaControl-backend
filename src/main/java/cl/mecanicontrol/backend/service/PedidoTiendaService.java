package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PedidoTiendaService {

    private final PedidoTiendaRepository pedidoRepo;
    private final ProductosRepository productosRepo;
    private final MovimientoInventarioRepository movimientoRepo;
    private final TipoMovimientoRepository tipoMovimientoRepo;
    private final UsuarioRepository usuarioRepo;

    public PedidoTiendaService(PedidoTiendaRepository pedidoRepo,
                                ProductosRepository productosRepo,
                                MovimientoInventarioRepository movimientoRepo,
                                TipoMovimientoRepository tipoMovimientoRepo,
                                UsuarioRepository usuarioRepo) {
        this.pedidoRepo       = pedidoRepo;
        this.productosRepo    = productosRepo;
        this.movimientoRepo   = movimientoRepo;
        this.tipoMovimientoRepo = tipoMovimientoRepo;
        this.usuarioRepo      = usuarioRepo;
    }

    @Transactional
    public PedidoTienda registrar(Map<String, Object> body, String usuarioEmail) {

        // ── Datos del cliente ──
        Map<String, Object> cliente = (Map<String, Object>) body.get("cliente");
        List<Map<String, Object>> itemsReq = (List<Map<String, Object>>) body.get("items");

        if (cliente == null || itemsReq == null || itemsReq.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        String numeroPedido = (String) body.get("numeroPedido");
        if (numeroPedido == null || numeroPedido.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Número de pedido requerido");
        }

        // tipo SALIDA para el movimiento de inventario
        TipoMovimiento tipoSalida = tipoMovimientoRepo.findByNombreIgnoreCase("SALIDA").orElse(null);

        // ── Crear pedido ──
        PedidoTienda pedido = new PedidoTienda();
        pedido.setNumeroPedido(numeroPedido);
        pedido.setClienteNombre(str(cliente, "nombre"));
        pedido.setClienteEmail(str(cliente, "email"));
        pedido.setClienteTelefono(str(cliente, "telefono"));
        pedido.setClienteDireccion(str(cliente, "direccion"));
        pedido.setClienteCiudad(str(cliente, "ciudad"));
        pedido.setClienteRegion(str(cliente, "region"));
        pedido.setNotas(str(cliente, "notas"));

        BigDecimal subtotal = toBD(body.get("subtotal"));
        BigDecimal envio    = toBD(body.get("envio"));
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(envio);
        pedido.setTotal(subtotal.add(envio));

        // asociar usuario si está autenticado
        if (usuarioEmail != null) {
            usuarioRepo.findByEmail(usuarioEmail).ifPresent(pedido::setUsuario);
        }

        // ── Procesar ítems + reducir stock ──
        for (Map<String, Object> item : itemsReq) {
            String productoIdStr = str(item, "id");
            int cantidad = toInt(item.get("cantidad"));
            BigDecimal precioUnit = toBD(item.get("precioVenta"));
            String nombreProducto = str(item, "nombre");

            PedidoTiendaItem pi = new PedidoTiendaItem();
            pi.setPedido(pedido);
            pi.setCantidad(cantidad);
            pi.setPrecioUnitario(precioUnit);
            pi.setSubtotal(precioUnit.multiply(BigDecimal.valueOf(cantidad)));
            pi.setNombreProducto(nombreProducto);

            // Reducir stock
            if (productoIdStr != null && !productoIdStr.isBlank()) {
                try {
                    UUID pid = UUID.fromString(productoIdStr);
                    Productos producto = productosRepo.findById(pid).orElse(null);
                    if (producto != null) {
                        int stockActual = producto.getStockActual() != null ? producto.getStockActual() : 0;
                        if (stockActual < cantidad) {
                            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                    "Stock insuficiente para: " + producto.getNombreProducto());
                        }
                        producto.setStockActual(stockActual - cantidad);
                        productosRepo.save(producto);
                        pi.setProducto(producto);

                        // Registrar movimiento de inventario
                        if (tipoSalida != null) {
                            MovimientoInventario mov = new MovimientoInventario();
                            mov.setProducto(producto);
                            mov.setTipoMovimiento(tipoSalida);
                            mov.setCantidad(cantidad);
                            mov.setPrecioUnitario(precioUnit);
                            mov.setMotivo("Venta tienda online — pedido " + numeroPedido);
                            if (pedido.getUsuario() != null) mov.setUsuario(pedido.getUsuario());
                            movimientoRepo.save(mov);
                        }
                    }
                } catch (IllegalArgumentException ignored) {}
            }

            pedido.getItems().add(pi);
        }

        return pedidoRepo.save(pedido);
    }

    // ── helpers ──
    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v != null ? v.toString() : null;
    }

    private BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private int toInt(Object v) {
        if (v == null) return 0;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
}