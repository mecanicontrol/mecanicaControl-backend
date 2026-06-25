package cl.mecanicontrol.backend.controller.admin;

import cl.mecanicontrol.backend.entity.OrdenTrabajo;
import cl.mecanicontrol.backend.entity.PedidoTienda;
import cl.mecanicontrol.backend.repository.OrdenTrabajoRepository;
import cl.mecanicontrol.backend.repository.PedidoTiendaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportesController {

    private final PedidoTiendaRepository pedidoRepo;
    private final OrdenTrabajoRepository otRepo;

    public AdminReportesController(PedidoTiendaRepository pedidoRepo,
                                   OrdenTrabajoRepository otRepo) {
        this.pedidoRepo = pedidoRepo;
        this.otRepo     = otRepo;
    }

    // ── GET /api/admin/reportes/ventas ─────────────────────────────────────────
    @GetMapping("/ventas")
    public ResponseEntity<Map<String, Object>> ventas(
            @RequestParam(defaultValue = "30") int dias) {

        LocalDateTime desde = LocalDate.now().minusDays(dias - 1L).atStartOfDay();
        LocalDateTime hasta = LocalDate.now().plusDays(1).atStartOfDay();

        // ── Pedidos tienda ──
        List<PedidoTienda> pedidos = pedidoRepo.findByFechaBetweenOrderByFechaDesc(desde, hasta);
        BigDecimal totalVentas = pedidos.stream()
                .map(PedidoTienda::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> pedidosMap = pedidos.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",            p.getId().toString());
            m.put("numeroPedido",  p.getNumeroPedido());
            m.put("fecha",         p.getFecha().toString());
            m.put("clienteNombre", p.getClienteNombre());
            m.put("clienteEmail",  p.getClienteEmail());
            m.put("subtotal",      p.getSubtotal());
            m.put("costoEnvio",    p.getCostoEnvio());
            m.put("total",         p.getTotal());
            m.put("items", p.getItems().stream().map(it -> {
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("nombre",    it.getNombreProducto());
                im.put("cantidad",  it.getCantidad());
                im.put("precio",    it.getPrecioUnitario());
                im.put("subtotal",  it.getSubtotal());
                return im;
            }).toList());
            return m;
        }).toList();

        // ── OTs completadas ──
        List<OrdenTrabajo> ots = otRepo.findByEstadoOtNombreEager("COMPLETADA").stream()
                .filter(ot -> ot.getFechaCierre() != null
                        && !ot.getFechaCierre().isBefore(desde)
                        && ot.getFechaCierre().isBefore(hasta))
                .sorted(Comparator.comparing(OrdenTrabajo::getFechaCierre).reversed())
                .collect(Collectors.toList());

        BigDecimal totalServicios = ots.stream()
                .map(ot -> {
                    BigDecimal mo = ot.getCostoManoObra() != null ? ot.getCostoManoObra() : BigDecimal.ZERO;
                    BigDecimal rp = ot.getCostoRepuestos() != null ? ot.getCostoRepuestos() : BigDecimal.ZERO;
                    return mo.add(rp);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> otsMap = ots.stream().map(ot -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("codigo",        ot.getCodigoOt());
            m.put("fechaCierre",   ot.getFechaCierre() != null ? ot.getFechaCierre().toString() : null);
            m.put("costoManoObra", ot.getCostoManoObra());
            m.put("costoRepuestos",ot.getCostoRepuestos());
            BigDecimal mo = ot.getCostoManoObra() != null ? ot.getCostoManoObra() : BigDecimal.ZERO;
            BigDecimal rp = ot.getCostoRepuestos() != null ? ot.getCostoRepuestos() : BigDecimal.ZERO;
            m.put("total",         mo.add(rp));
            var vehiculo = ot.getAgendamiento() != null ? ot.getAgendamiento().getIdVehiculo() : null;
            if (vehiculo != null) {
                m.put("patente",  vehiculo.getPatente() != null ? vehiculo.getPatente() : "—");
                String desc = vehiculo.getAlias() != null ? vehiculo.getAlias()
                            : vehiculo.getAnio()  != null ? vehiculo.getAnio().toString() : "—";
                m.put("vehiculo", desc);
            } else {
                m.put("patente", "—"); m.put("vehiculo", "—");
            }
            return m;
        }).toList();

        // ── Resumen ──
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalVentasTienda",   totalVentas);
        resumen.put("totalServicios",      totalServicios);
        resumen.put("totalGeneral",        totalVentas.add(totalServicios));
        resumen.put("cantidadPedidos",     pedidos.size());
        resumen.put("cantidadOTs",         ots.size());
        resumen.put("dias",                dias);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("resumen",  resumen);
        resp.put("pedidos",  pedidosMap);
        resp.put("servicios",otsMap);
        return ResponseEntity.ok(resp);
    }
}