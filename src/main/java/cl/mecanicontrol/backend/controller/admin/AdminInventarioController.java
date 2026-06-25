package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.entity.*;
import cl.mecanicontrol.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/inventario")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventarioController {

    private final ProductosRepository          productosRepo;
    private final CategoriaProductoRepository  categoriaRepo;
    private final MarcaProductoRepository      marcaRepo;
    private final TipoMovimientoRepository     tipoMovRepo;
    private final MovimientoInventarioRepository movRepo;
    private final UsuarioRepository            usuarioRepo;

    public AdminInventarioController(ProductosRepository productosRepo,
                                     CategoriaProductoRepository categoriaRepo,
                                     MarcaProductoRepository marcaRepo,
                                     TipoMovimientoRepository tipoMovRepo,
                                     MovimientoInventarioRepository movRepo,
                                     UsuarioRepository usuarioRepo) {
        this.productosRepo = productosRepo;
        this.categoriaRepo = categoriaRepo;
        this.marcaRepo     = marcaRepo;
        this.tipoMovRepo   = tipoMovRepo;
        this.movRepo       = movRepo;
        this.usuarioRepo   = usuarioRepo;
    }

    // ── Listar ───────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "todos") String filtro) {

        List<Productos> todos = productosRepo.findAll();

        if (!q.isBlank()) {
            String qLower = q.toLowerCase();
            todos = todos.stream()
                    .filter(p -> p.getNombreProducto() != null &&
                                 p.getNombreProducto().toLowerCase().contains(qLower))
                    .collect(Collectors.toList());
        }

        todos = switch (filtro) {
            case "activos"    -> todos.stream().filter(p ->  Boolean.TRUE.equals(p.getProductoActivo())).collect(Collectors.toList());
            case "inactivos"  -> todos.stream().filter(p -> !Boolean.TRUE.equals(p.getProductoActivo())).collect(Collectors.toList());
            case "sin_stock"  -> todos.stream().filter(p -> p.getStockActual() != null && p.getStockActual() <= 0).collect(Collectors.toList());
            case "stock_bajo" -> todos.stream().filter(p ->
                    p.getStockActual() != null && p.getStockMinimo() != null &&
                    p.getStockActual() > 0 && p.getStockActual() <= p.getStockMinimo()
            ).collect(Collectors.toList());
            default -> todos;
        };

        return ResponseEntity.ok(todos.stream().map(this::toMap).toList());
    }

    // ── Catalogos de apoyo ────────────────────────────────────────────────────

    @GetMapping("/categorias")
    public ResponseEntity<List<Map<String, Object>>> categorias() {
        return ResponseEntity.ok(categoriaRepo.findAll().stream().map(c -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id",     c.getId().toString());
            m.put("nombre", c.getNombre());
            return m;
        }).toList());
    }

    @GetMapping("/marcas")
    public ResponseEntity<List<Map<String, Object>>> marcas() {
        return ResponseEntity.ok(marcaRepo.findAll().stream().map(m -> {
            Map<String, Object> mp = new java.util.HashMap<>();
            mp.put("id",     m.getId().toString());
            mp.put("nombre", m.getNombre());
            return mp;
        }).toList());
    }

    @GetMapping("/tipos-movimiento")
    public ResponseEntity<List<Map<String, Object>>> tiposMovimiento() {
        return ResponseEntity.ok(tipoMovRepo.findAll().stream().map(t -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id",     t.getId().toString());
            m.put("nombre", t.getNombre());
            m.put("signo",  t.getSigno());
            return m;
        }).toList());
    }

    // ── Crear producto ────────────────────────────────────────────────────────

    @Transactional
    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        validarRequeridos(body);

        Productos p = new Productos();
        mapearDesdeBody(p, body);
        p.setStockActual(0);   // el trigger actualizará si se crea un movimiento inicial
        p.setProductoActivo(true);
        Productos saved = productosRepo.save(p);

        // Si viene stock inicial > 0, creamos un movimiento ENTRADA
        int stockInicial = parseInt(body.get("stockActual"));
        if (stockInicial > 0) {
            registrarMovimiento(saved, "ENTRADA", stockInicial,
                    parseBD(body.get("precioVenta")),
                    "Stock inicial al crear el producto", userDetails);
            saved = productosRepo.findById(saved.getIdProducto()).orElse(saved);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(saved));
    }

    // ── Actualizar producto ───────────────────────────────────────────────────

    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        Productos p = productosRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        validarRequeridos(body);
        mapearDesdeBody(p, body);
        // No tocamos stock_actual aquí — se gestiona vía movimientos
        return ResponseEntity.ok(toMap(productosRepo.save(p)));
    }

    // ── Toggle activo ─────────────────────────────────────────────────────────

    @Transactional
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleActivo(@PathVariable UUID id) {
        Productos p = productosRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        p.setProductoActivo(!Boolean.TRUE.equals(p.getProductoActivo()));
        productosRepo.save(p);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("activo", p.getProductoActivo());
        return ResponseEntity.ok(resp);
    }

    // ── Ajustar stock via movimiento ──────────────────────────────────────────

    @Transactional
    @PostMapping("/{id}/movimiento")
    public ResponseEntity<Map<String, Object>> registrarMovimientoEndpoint(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Productos p = productosRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        String tipoNombre = body.getOrDefault("tipo", "AJUSTE").toString();
        int cantidad      = Math.abs(parseInt(body.get("cantidad")));
        if (cantidad <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a 0");

        String motivo = body.containsKey("motivo") ? body.get("motivo").toString() : null;

        registrarMovimiento(p, tipoNombre, cantidad, p.getPrecioVenta(), motivo, userDetails);

        Productos actualizado = productosRepo.findById(id).orElse(p);
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("stock",  actualizado.getStockActual());
        resp.put("activo", actualizado.getProductoActivo());
        return ResponseEntity.ok(resp);
    }

    // ── Historial de movimientos ──────────────────────────────────────────────

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<Map<String, Object>>> historialMovimientos(@PathVariable UUID id) {
        productosRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        List<Map<String, Object>> result = movRepo
                .findByProductoIdProductoOrderByCreatedAtDesc(id)
                .stream()
                .map(m -> {
                    Map<String, Object> mm = new java.util.HashMap<>();
                    mm.put("id",          m.getId().toString());
                    mm.put("tipo",        m.getTipoMovimiento().getNombre());
                    mm.put("signo",       m.getTipoMovimiento().getSigno());
                    mm.put("cantidad",    m.getCantidad());
                    mm.put("precio",      m.getPrecioUnitario());
                    mm.put("motivo",      m.getMotivo() != null ? m.getMotivo() : "");
                    mm.put("fecha",       m.getCreatedAt() != null ? m.getCreatedAt().toString() : "");
                    mm.put("usuario",     m.getUsuario() != null
                            ? m.getUsuario().getNombre() + " " + m.getUsuario().getApellido()
                            : "Sistema");
                    return mm;
                }).toList();
        return ResponseEntity.ok(result);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        Productos p = productosRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        productosRepo.delete(p);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validarRequeridos(Map<String, Object> body) {
        if (!body.containsKey("nombre") || body.get("nombre") == null ||
                body.get("nombre").toString().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio");

        if (!body.containsKey("sku") || body.get("sku") == null ||
                body.get("sku").toString().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código SKU es obligatorio y debe ser único");

        if (!body.containsKey("categoriaId") || body.get("categoriaId") == null ||
                body.get("categoriaId").toString().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría del producto es obligatoria");
    }

    private void mapearDesdeBody(Productos p, Map<String, Object> body) {
        p.setNombreProducto(body.get("nombre").toString().trim());
        p.setSku(body.get("sku").toString().trim());

        if (body.containsKey("descripcion"))
            p.setDescripcionProduct(body.get("descripcion") != null ? body.get("descripcion").toString() : null);
        if (body.containsKey("precioCosto"))
            p.setPrecioCosto(parseBD(body.get("precioCosto")));
        if (body.containsKey("precioVenta"))
            p.setPrecioVenta(parseBD(body.get("precioVenta")));
        if (body.containsKey("stockMinimo"))
            p.setStockMinimo(parseInt(body.get("stockMinimo")));
        if (body.containsKey("ubicacionBodega"))
            p.setUbicacionBodega(body.get("ubicacionBodega") != null ? body.get("ubicacionBodega").toString() : null);

        // Categoría obligatoria
        CategoriaProducto cat = categoriaRepo
                .findById(UUID.fromString(body.get("categoriaId").toString()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no encontrada"));
        p.setCategoriaProducto(cat);

        // Marca opcional
        if (body.containsKey("marcaId") && body.get("marcaId") != null && !body.get("marcaId").toString().isBlank()) {
            marcaRepo.findById(UUID.fromString(body.get("marcaId").toString()))
                    .ifPresent(p::setMarcaProducto);
        }
    }

    private void registrarMovimiento(Productos producto, String tipoNombre, int cantidad,
                                     BigDecimal precio, String motivo, UserDetails userDetails) {
        TipoMovimiento tipo = tipoMovRepo.findAll().stream()
                .filter(t -> t.getNombre().equalsIgnoreCase(tipoNombre))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tipo de movimiento no encontrado: " + tipoNombre + ". Ejecuta el script de datos iniciales."));

        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setPrecioUnitario(precio != null ? precio : BigDecimal.ZERO);
        mov.setMotivo(motivo);

        if (userDetails != null) {
            usuarioRepo.findByEmail(userDetails.getUsername()).ifPresent(mov::setUsuario);
        }

        movRepo.save(mov);
        // El trigger trg_actualizar_stock en la BD actualiza stock_actual automáticamente
    }

    private Map<String, Object> toMap(Productos p) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("id",          p.getIdProducto().toString());
        m.put("nombre",      p.getNombreProducto() != null ? p.getNombreProducto() : "");
        m.put("sku",         p.getSku() != null ? p.getSku() : "");
        m.put("descripcion", p.getDescripcionProduct() != null ? p.getDescripcionProduct() : "");
        m.put("precioCosto", p.getPrecioCosto() != null ? p.getPrecioCosto() : BigDecimal.ZERO);
        m.put("precioVenta", p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO);
        m.put("stock",       p.getStockActual() != null ? p.getStockActual() : 0);
        m.put("stockMinimo", p.getStockMinimo() != null ? p.getStockMinimo() : 0);
        m.put("ubicacion",   p.getUbicacionBodega() != null ? p.getUbicacionBodega() : "");
        m.put("activo",      Boolean.TRUE.equals(p.getProductoActivo()));
        m.put("categoria",   p.getCategoriaProducto() != null ? p.getCategoriaProducto().getNombre() : "");
        m.put("categoriaId", p.getCategoriaProducto() != null ? p.getCategoriaProducto().getId().toString() : null);
        m.put("marca",       p.getMarcaProducto() != null ? p.getMarcaProducto().getNombre() : "");
        m.put("marcaId",     p.getMarcaProducto() != null ? p.getMarcaProducto().getId().toString() : null);
        return m;
    }

    private BigDecimal parseBD(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private Integer parseInt(Object val) {
        if (val == null) return 0;
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }
}
