package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.Productos;
import cl.mecanicontrol.backend.entity.PedidoTienda;
import cl.mecanicontrol.backend.repository.ProductosRepository;
import cl.mecanicontrol.backend.service.PedidoTiendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tienda")
public class TiendaController {

    private final ProductosRepository productosRepo;
    private final PedidoTiendaService pedidoService;

    public TiendaController(ProductosRepository productosRepo, PedidoTiendaService pedidoService) {
        this.productosRepo = productosRepo;
        this.pedidoService = pedidoService;
    }

    @PostMapping("/pedido")
    public ResponseEntity<Map<String, Object>> registrarPedido(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        PedidoTienda pedido = pedidoService.registrar(body, email);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id",            pedido.getId().toString());
        resp.put("numeroPedido",  pedido.getNumeroPedido());
        resp.put("fecha",         pedido.getFecha().toString());
        resp.put("total",         pedido.getTotal());
        resp.put("ok",            true);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Map<String, Object>>> listar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String categoria) {

        List<Productos> productos = productosRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getProductoActivo()))
                .filter(p -> p.getStockActual() != null && p.getStockActual() > 0)
                .collect(Collectors.toList());

        if (!q.isBlank()) {
            String qLower = q.toLowerCase();
            productos = productos.stream()
                    .filter(p -> (p.getNombreProducto() != null && p.getNombreProducto().toLowerCase().contains(qLower))
                            || (p.getDescripcionProduct() != null && p.getDescripcionProduct().toLowerCase().contains(qLower))
                            || (p.getCategoriaProducto() != null && p.getCategoriaProducto().getNombre().toLowerCase().contains(qLower)))
                    .collect(Collectors.toList());
        }

        if (!categoria.isBlank() && !categoria.equalsIgnoreCase("TODOS")) {
            productos = productos.stream()
                    .filter(p -> p.getCategoriaProducto() != null &&
                            p.getCategoriaProducto().getNombre().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(productos.stream().map(this::toMap).toList());
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> categorias() {
        List<String> cats = productosRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getProductoActivo())
                        && p.getStockActual() != null && p.getStockActual() > 0
                        && p.getCategoriaProducto() != null)
                .map(p -> p.getCategoriaProducto().getNombre())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return ResponseEntity.ok(cats);
    }

    private Map<String, Object> toMap(Productos p) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("id",          p.getIdProducto().toString());
        m.put("nombre",      p.getNombreProducto() != null ? p.getNombreProducto() : "");
        m.put("sku",         p.getSku() != null ? p.getSku() : "");
        m.put("descripcion", p.getDescripcionProduct() != null ? p.getDescripcionProduct() : "");
        m.put("precioVenta", p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO);
        m.put("stock",       p.getStockActual() != null ? p.getStockActual() : 0);
        m.put("categoria",   p.getCategoriaProducto() != null ? p.getCategoriaProducto().getNombre() : "");
        m.put("marca",       p.getMarcaProducto() != null ? p.getMarcaProducto().getNombre() : "");
        return m;
    }
}
