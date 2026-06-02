package cl.mecanicontrol.backend.controller;

import cl.mecanicontrol.backend.entity.EstadoOt;
import cl.mecanicontrol.backend.entity.FaseVehiculo;
import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.PerfilUsuario;
import cl.mecanicontrol.backend.entity.Productos;
import cl.mecanicontrol.backend.entity.RepuestoOT;
import cl.mecanicontrol.backend.entity.Tecnico;
import cl.mecanicontrol.backend.entity.Usuario;
import cl.mecanicontrol.backend.repository.EstadoOtRepository;
import cl.mecanicontrol.backend.repository.FaseVehiculoRepository;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.repository.PerfilUsuarioRepository;
import cl.mecanicontrol.backend.repository.ProductosRepository;
import cl.mecanicontrol.backend.repository.RepuestoOTRepository;
import cl.mecanicontrol.backend.repository.TecnicoRepository;
import cl.mecanicontrol.backend.repository.UsuarioRepository;
import cl.mecanicontrol.backend.service.GroqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;

@RestController
@RequestMapping("/api/tecnicos")
@PreAuthorize("hasRole('TECNICO')")
public class TecnicoController {

    private final UsuarioRepository usuarioRepo;
    private final TecnicoRepository tecnicoRepo;
    private final PerfilUsuarioRepository perfilRepo;
    private final OrdenTrabajoRepository otRepo;
    private final EstadoOtRepository estadoOtRepo;
    private final FaseVehiculoRepository faseRepo;
    private final RepuestoOTRepository repuestoRepo;
    private final ProductosRepository productosRepo;
    private final GroqService groqService;

    public TecnicoController(UsuarioRepository usuarioRepo,
                             TecnicoRepository tecnicoRepo,
                             PerfilUsuarioRepository perfilRepo,
                             OrdenTrabajoRepository otRepo,
                             EstadoOtRepository estadoOtRepo,
                             FaseVehiculoRepository faseRepo,
                             RepuestoOTRepository repuestoRepo,
                             ProductosRepository productosRepo,
                             GroqService groqService) {
        this.usuarioRepo   = usuarioRepo;
        this.tecnicoRepo   = tecnicoRepo;
        this.perfilRepo    = perfilRepo;
        this.otRepo        = otRepo;
        this.estadoOtRepo  = estadoOtRepo;
        this.faseRepo      = faseRepo;
        this.repuestoRepo  = repuestoRepo;
        this.productosRepo = productosRepo;
        this.groqService   = groqService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> miPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepo.findByIdUsuarioId(usuario.getId()).orElse(null);
        PerfilUsuario perfil = perfilRepo.findByUsuarioId(usuario.getId()).orElse(null);

        return ResponseEntity.ok(Map.of(
                "nombre",        usuario.getNombre(),
                "apellido",      usuario.getApellido() != null ? usuario.getApellido() : "",
                "email",         usuario.getEmail(),
                "telefono",      perfil != null && perfil.getTelefono() != null ? perfil.getTelefono() : "",
                "direccion",     perfil != null && perfil.getDireccion() != null ? perfil.getDireccion() : "",
                "nivel",         tecnico != null && tecnico.getIdNivelTecnico() != null ? tecnico.getIdNivelTecnico().getNombre() : "",
                "disponible",    tecnico != null && tecnico.getDisponible() != null ? tecnico.getDisponible() : true,
                "especialidades","",
                "foto",          perfil != null && perfil.getFotoUrl() != null ? perfil.getFotoUrl() : ""
        ));
    }

    @Transactional
    @PatchMapping("/me")
    public ResponseEntity<Void> actualizarPerfil(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        PerfilUsuario perfil = perfilRepo.findByUsuarioId(usuario.getId())
                .orElseGet(() -> { PerfilUsuario p = new PerfilUsuario(); p.setUsuario(usuario); return p; });

        if (body.containsKey("telefono"))    perfil.setTelefono(body.get("telefono").toString());
        if (body.containsKey("direccion"))   perfil.setDireccion(body.get("direccion").toString());
        if (body.containsKey("fotoUrl"))     perfil.setFotoUrl(body.get("fotoUrl").toString());
        perfilRepo.save(perfil);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PatchMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> toggleDisponibilidad(
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        boolean nuevo = tecnico.getDisponible() == null || !tecnico.getDisponible();
        tecnico.setDisponible(nuevo);
        tecnicoRepo.save(tecnico);

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("disponible", nuevo);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/inventario")
    public ResponseEntity<List<Map<String, Object>>> listarInventario(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        List<Productos> todos = q.isBlank()
                ? productosRepo.findAll().stream().filter(p -> Boolean.TRUE.equals(p.getProductoActivo())).toList()
                : productosRepo.findTop20ByNombreProductoContainingIgnoreCaseAndProductoActivoTrue(q);

        int desde = page * size;
        int hasta = Math.min(desde + size, todos.size());
        List<Productos> pagina = desde >= todos.size() ? List.of() : todos.subList(desde, hasta);

        List<Map<String, Object>> result = pagina.stream().map(p -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id",          p.getIdProducto().toString());
            m.put("nombre",      p.getNombreProducto() != null ? p.getNombreProducto() : "");
            m.put("sku",         p.getSku() != null ? p.getSku() : "");
            m.put("precio",      p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO);
            m.put("stock",       p.getStockActual() != null ? p.getStockActual() : 0);
            m.put("stockMin",    p.getStockMinimo() != null ? p.getStockMinimo() : 0);
            m.put("descripcion", p.getDescripcionProduct() != null ? p.getDescripcionProduct() : "");
            m.put("ubicacion",   p.getUbicacionBodega() != null ? p.getUbicacionBodega() : "");
            m.put("categoria",   p.getCategoriaProducto() != null ? p.getCategoriaProducto().getNombre() : "");
            m.put("marca",       p.getMarcaProducto() != null ? p.getMarcaProducto().getNombre() : "");
            m.put("total",       todos.size());
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepo.findByIdUsuarioId(usuario.getId()).orElse(null);

        if (tecnico == null) {
            return ResponseEntity.ok(Map.of(
                    "tiempoPromedio", 0,
                    "otActivas",     0,
                    "otCompletadas", 0
            ));
        }

        List<OrdenTrabajo> completadas = otRepo.findByTecnicoIdTecnico(tecnico.getIdTecnico())
                .stream()
                .filter(ot -> ot.getFechaCierre() != null && ot.getFechaInicio() != null)
                .toList();

        OptionalDouble promedioMin = completadas.stream()
                .mapToLong(ot -> Duration.between(ot.getFechaInicio(), ot.getFechaCierre()).toMinutes())
                .filter(m -> m > 0)
                .average();

        long tiempoPromedio = promedioMin.isPresent() ? Math.round(promedioMin.getAsDouble()) : 0;

        return ResponseEntity.ok(Map.of(
                "tiempoPromedio", tiempoPromedio,
                "otActivas",     otRepo.findByTecnicoIdTecnico(tecnico.getIdTecnico()).stream()
                                       .filter(ot -> ot.getFechaCierre() == null).count(),
                "otCompletadas", completadas.size()
        ));
    }

    @GetMapping("/ordenes")
    public ResponseEntity<List<Map<String, Object>>> misOrdenes(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepo.findByIdUsuarioId(usuario.getId()).orElse(null);
        if (tecnico == null) return ResponseEntity.ok(List.of());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Map<String, Object>> result = otRepo.findResumenByTecnicoId(tecnico.getIdTecnico())
                .stream()
                .map(row -> {
                    String fecha = "";
                    if (row[1] instanceof LocalDateTime ldt) fecha = ldt.format(fmt);
                    else if (row[1] != null) fecha = row[1].toString().substring(0, 10);

                    String marca  = row[5] != null ? row[5].toString() : "";
                    String modelo = row[6] != null ? row[6].toString() : "";
                    String vehiculoNombre = (marca + " " + modelo).trim();

                    String clienteNombre = row[7] != null ? row[7].toString() : "";
                    String clienteApe    = row[8] != null ? row[8].toString() : "";
                    String cliente = (clienteNombre + " " + clienteApe).trim();

                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("codigo",   row[0] != null ? row[0].toString() : "");
                    m.put("fecha",    fecha);
                    m.put("estado",   row[2] != null ? row[2].toString() : "");
                    m.put("servicio", row[3] != null ? row[3].toString() : "");
                    m.put("patente",  row[4] != null ? row[4].toString() : "");
                    m.put("vehiculo", vehiculoNombre);
                    m.put("cliente",  cliente);
                    return m;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/ordenes/{codigo}")
    public ResponseEntity<Map<String, Object>> detalleOrden(
            @PathVariable String codigo,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepo.findByIdUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin perfil técnico"));

        List<Object[]> rows = otRepo.findDetalleByCodigoOt(codigo);
        if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada");
        Object[] row = rows.get(0);

        // row[14] = tecnico_id — verify ownership
        if (row[14] == null || !row[14].toString().equals(tecnico.getIdTecnico().toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Orden no asignada a este técnico");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String marca  = row[5] != null ? row[5].toString() : "";
        String modelo = row[6] != null ? row[6].toString() : "";
        String clienteNombre = row[7] != null ? row[7].toString() : "";
        String clienteApe    = row[8] != null ? row[8].toString() : "";

        String fechaInicio = "";
        if (row[10] instanceof java.time.LocalDateTime ldt) fechaInicio = ldt.format(fmt);
        else if (row[10] != null) fechaInicio = row[10].toString().substring(0, 16).replace("T", " ");

        String fechaCierre = "";
        if (row[11] instanceof java.time.LocalDateTime ldt2) fechaCierre = ldt2.format(fmt);
        else if (row[11] != null) fechaCierre = row[11].toString().substring(0, 16).replace("T", " ");

        Map<String, Object> detalle = new java.util.HashMap<>();
        detalle.put("id",          row[0] != null ? row[0].toString() : "");
        detalle.put("codigo",      row[1] != null ? row[1].toString() : "");
        detalle.put("estado",      row[2] != null ? row[2].toString() : "");
        detalle.put("servicio",    row[3] != null ? row[3].toString() : "");
        detalle.put("patente",     row[4] != null ? row[4].toString() : "");
        detalle.put("vehiculo",    (marca + " " + modelo).trim());
        detalle.put("cliente",     (clienteNombre + " " + clienteApe).trim());
        detalle.put("diagnostico", row[9] != null ? row[9].toString() : "");
        detalle.put("fechaInicio", fechaInicio);
        detalle.put("fechaCierre", fechaCierre);
        detalle.put("costoManoObra",  row[12] != null ? row[12].toString() : "0");
        detalle.put("costoRepuestos", row[13] != null ? row[13].toString() : "0");

        return ResponseEntity.ok(detalle);
    }

    @Transactional
    @PatchMapping("/ordenes/{codigo}/diagnostico")
    public ResponseEntity<Void> guardarDiagnostico(
            @PathVariable String codigo,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepo.findByIdUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin perfil técnico"));

        OrdenTrabajo ot = otRepo.findByCodigoOt(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        if (ot.getTecnico() == null || !ot.getTecnico().getIdTecnico().equals(tecnico.getIdTecnico())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Orden no asignada a este técnico");
        }

        ot.setDiagnostico(body.get("diagnostico"));
        otRepo.save(ot);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PatchMapping("/ordenes/{codigo}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(
            @PathVariable String codigo,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Tecnico tecnico = tecnicoRepo.findByIdUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin perfil técnico"));

        OrdenTrabajo ot = otRepo.findByCodigoOt(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        if (ot.getTecnico() == null || !ot.getTecnico().getIdTecnico().equals(tecnico.getIdTecnico())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Orden no asignada a este técnico");
        }

        String nuevoEstado = body.get("estado");
        EstadoOt estadoOt = estadoOtRepo.findByNombre(nuevoEstado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: " + nuevoEstado));

        ot.setEstadoOt(estadoOt);
        if ("COMPLETADA".equals(nuevoEstado) && ot.getFechaCierre() == null) {
            ot.setFechaCierre(java.time.LocalDateTime.now());
        }
        otRepo.save(ot);

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("estado", nuevoEstado);
        return ResponseEntity.ok(resp);
    }

    // ── Fases ────────────────────────────────────────────────────────────────

    @GetMapping("/ordenes/{codigo}/fases")
    public ResponseEntity<List<Map<String, Object>>> getFases(
            @PathVariable String codigo,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        OrdenTrabajo ot = otRepo.findByCodigoOt(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        verificarPropiedad(ot, tecnico);

        List<FaseVehiculo> fases = faseRepo.findByOrdenTrabajoId(ot.getId())
                .stream()
                .sorted((a, b) -> a.getFase().getOrden().compareTo(b.getFase().getOrden()))
                .toList();

        // Determinar cuál es la fase activa (primera sin finAt)
        boolean encontroActiva = false;
        List<Map<String, Object>> result = new ArrayList<>();
        for (FaseVehiculo fv : fases) {
            String estadoFase;
            if (fv.getFinAt() != null) {
                estadoFase = "COMPLETADA";
            } else if (!encontroActiva) {
                estadoFase = "ACTIVA";
                encontroActiva = true;
            } else {
                estadoFase = "PENDIENTE";
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id",           fv.getId().toString());
            m.put("nombre",       fv.getFase().getNombre());
            m.put("orden",        fv.getFase().getOrden());
            m.put("estado",       estadoFase);
            m.put("observaciones", fv.getObservaciones() != null ? fv.getObservaciones() : "");
            m.put("imagenes",     fv.getImagenes() != null ? fv.getImagenes() : "[]");
            m.put("inicioAt",     fv.getInicioAt() != null ? fv.getInicioAt().format(fmt) : null);
            m.put("finAt",        fv.getFinAt() != null ? fv.getFinAt().format(fmt) : null);
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @Transactional
    @PatchMapping("/fases/{faseId}")
    public ResponseEntity<Void> guardarFase(
            @PathVariable UUID faseId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        FaseVehiculo fv = faseRepo.findById(faseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fase no encontrada"));
        verificarPropiedad(fv.getOrdenTrabajo(), tecnico);

        if (body.containsKey("observaciones"))
            fv.setObservaciones(body.get("observaciones").toString());
        if (body.containsKey("imagenes"))
            fv.setImagenes(body.get("imagenes").toString());
        faseRepo.save(fv);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/fases/{faseId}/completar")
    public ResponseEntity<Void> completarFase(
            @PathVariable UUID faseId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        FaseVehiculo fv = faseRepo.findById(faseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fase no encontrada"));
        verificarPropiedad(fv.getOrdenTrabajo(), tecnico);

        if (fv.getFinAt() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La fase ya está completada");

        if (body.containsKey("observaciones"))
            fv.setObservaciones(body.get("observaciones").toString());
        if (body.containsKey("imagenes"))
            fv.setImagenes(body.get("imagenes").toString());
        fv.setFinAt(LocalDateTime.now());
        faseRepo.save(fv);
        return ResponseEntity.noContent().build();
    }

    // ── Repuestos ─────────────────────────────────────────────────────────────

    @GetMapping("/ordenes/{codigo}/repuestos")
    public ResponseEntity<List<Map<String, Object>>> getRepuestos(
            @PathVariable String codigo,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        OrdenTrabajo ot = otRepo.findByCodigoOt(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        verificarPropiedad(ot, tecnico);

        List<Map<String, Object>> result = repuestoRepo.findByOrdenTrabajoIdOrderByCreadoAtAsc(ot.getId())
                .stream()
                .map(r -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id",             r.getId().toString());
                    m.put("nombre",         r.getNombre());
                    m.put("cantidad",       r.getCantidad());
                    m.put("precioUnitario", r.getPrecioUnitario());
                    m.put("origen",         r.getOrigen());
                    m.put("productoId",     r.getProductoId() != null ? r.getProductoId().toString() : null);
                    return m;
                }).toList();
        return ResponseEntity.ok(result);
    }

    @Transactional
    @PostMapping("/ordenes/{codigo}/repuestos")
    public ResponseEntity<Map<String, Object>> addRepuesto(
            @PathVariable String codigo,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        OrdenTrabajo ot = otRepo.findByCodigoOt(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        verificarPropiedad(ot, tecnico);

        RepuestoOT r = new RepuestoOT();
        r.setOrdenTrabajo(ot);
        r.setNombre(body.get("nombre").toString());
        r.setCantidad(body.containsKey("cantidad") ? Integer.valueOf(body.get("cantidad").toString()) : 1);
        r.setPrecioUnitario(body.containsKey("precioUnitario")
                ? new BigDecimal(body.get("precioUnitario").toString()) : BigDecimal.ZERO);
        r.setOrigen(body.containsKey("origen") ? body.get("origen").toString() : "MECANIHUB");
        if (body.containsKey("productoId") && body.get("productoId") != null)
            r.setProductoId(UUID.fromString(body.get("productoId").toString()));

        RepuestoOT saved = repuestoRepo.save(r);

        // Actualizar costoRepuestos en la OT
        BigDecimal totalRepuestos = repuestoRepo.findByOrdenTrabajoIdOrderByCreadoAtAsc(ot.getId())
                .stream()
                .map(rep -> rep.getPrecioUnitario().multiply(BigDecimal.valueOf(rep.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ot.setCostoRepuestos(totalRepuestos);
        otRepo.save(ot);

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("id",             saved.getId().toString());
        resp.put("nombre",         saved.getNombre());
        resp.put("cantidad",       saved.getCantidad());
        resp.put("precioUnitario", saved.getPrecioUnitario());
        resp.put("origen",         saved.getOrigen());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Transactional
    @DeleteMapping("/repuestos/{id}")
    public ResponseEntity<Void> deleteRepuesto(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Tecnico tecnico = resolverTecnico(userDetails.getUsername());
        RepuestoOT r = repuestoRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repuesto no encontrado"));
        verificarPropiedad(r.getOrdenTrabajo(), tecnico);

        OrdenTrabajo ot = r.getOrdenTrabajo();
        repuestoRepo.delete(r);

        BigDecimal totalRepuestos = repuestoRepo.findByOrdenTrabajoIdOrderByCreadoAtAsc(ot.getId())
                .stream()
                .map(rep -> rep.getPrecioUnitario().multiply(BigDecimal.valueOf(rep.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ot.setCostoRepuestos(totalRepuestos);
        otRepo.save(ot);

        return ResponseEntity.noContent().build();
    }

    // ── Inventario ────────────────────────────────────────────────────────────

    @GetMapping("/productos")
    public ResponseEntity<List<Map<String, Object>>> buscarProductos(
            @RequestParam(defaultValue = "") String q) {

        List<Productos> productos = q.isBlank()
                ? List.of()
                : productosRepo.findTop20ByNombreProductoContainingIgnoreCaseAndProductoActivoTrue(q);

        List<Map<String, Object>> result = productos.stream().map(p -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id",      p.getIdProducto().toString());
            m.put("nombre",  p.getNombreProducto());
            m.put("sku",     p.getSku() != null ? p.getSku() : "");
            m.put("precio",  p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO);
            m.put("stock",   p.getStockActual() != null ? p.getStockActual() : 0);
            return m;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ── IA Chat ───────────────────────────────────────────────────────────────

    @PostMapping("/ia/chat")
    public ResponseEntity<Map<String, Object>> chatIA(@RequestBody Map<String, Object> body) {
        String vehiculo  = body.getOrDefault("vehiculo",  "").toString();
        String servicio  = body.getOrDefault("servicio",  "").toString();
        String pregunta  = body.getOrDefault("pregunta",  "").toString();
        String historial = body.getOrDefault("historial", "").toString();

        String prompt = "Eres MecaniBot, el asistente técnico experto del taller MecánicaHub. " +
                "Ayudas a técnicos mecánicos con diagnósticos, procedimientos, torques, códigos de falla y recomendaciones. " +
                "Responde de forma clara y concisa en español.\n\n" +
                "Vehículo en atención: " + vehiculo + "\n" +
                "Servicio actual: " + servicio + "\n\n" +
                (historial.isBlank() ? "" : "Conversación previa:\n" + historial + "\n\n") +
                "Pregunta del técnico: " + pregunta + "\n\n" +
                "Responde directamente sin preámbulos.";

        try {
            String respuesta = groqService.llamar(prompt);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("respuesta", respuesta);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("respuesta", "No pude conectarme al asistente en este momento. Intenta nuevamente.");
            return ResponseEntity.ok(resp);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Tecnico resolverTecnico(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return tecnicoRepo.findByIdUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin perfil técnico"));
    }

    private void verificarPropiedad(OrdenTrabajo ot, Tecnico tecnico) {
        if (ot.getTecnico() == null || !ot.getTecnico().getIdTecnico().equals(tecnico.getIdTecnico()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Orden no asignada a este técnico");
    }
}
