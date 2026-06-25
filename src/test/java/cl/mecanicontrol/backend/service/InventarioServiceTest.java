package cl.mecanicontrol.backend.service;

import cl.mecanicontrol.backend.entity.Productos;
import cl.mecanicontrol.backend.repository.ProductosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock ProductosRepository productosRepo;

    @InjectMocks InventarioService inventarioService;

    private Productos productoConStock;
    private Productos productoSinStock;
    private Productos productoBajoMinimo;
    private Productos productoNormal;

    @BeforeEach
    void setUp() {
        productoConStock = new Productos();
        productoConStock.setIdProducto(UUID.randomUUID());
        productoConStock.setNombreProducto("Aceite 10W-40");
        productoConStock.setStockActual(20);
        productoConStock.setStockMinimo(5);
        productoConStock.setProductoActivo(true);
        productoConStock.setPrecioVenta(BigDecimal.valueOf(8500));

        productoSinStock = new Productos();
        productoSinStock.setIdProducto(UUID.randomUUID());
        productoSinStock.setNombreProducto("Filtro de aire");
        productoSinStock.setStockActual(3);
        productoSinStock.setStockMinimo(5);
        productoSinStock.setProductoActivo(true);
        productoSinStock.setPrecioVenta(BigDecimal.valueOf(12000));

        productoBajoMinimo = new Productos();
        productoBajoMinimo.setIdProducto(UUID.randomUUID());
        productoBajoMinimo.setNombreProducto("Pastillas de freno");
        productoBajoMinimo.setStockActual(2);
        productoBajoMinimo.setStockMinimo(4);
        productoBajoMinimo.setProductoActivo(true);
        productoBajoMinimo.setPrecioVenta(BigDecimal.valueOf(25000));

        productoNormal = new Productos();
        productoNormal.setIdProducto(UUID.randomUUID());
        productoNormal.setNombreProducto("Líquido de frenos");
        productoNormal.setStockActual(15);
        productoNormal.setStockMinimo(3);
        productoNormal.setProductoActivo(true);
        productoNormal.setPrecioVenta(BigDecimal.valueOf(5000));
    }

    // ── TU-INV-01 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-INV-01: validarSalidaStock con stock suficiente no lanza excepción")
    void movimientoSalida_conStockSuficiente_noLanzaExcepcion() {
        // Arrange — stock=20, solicitamos 5
        // Act + Assert — no debe lanzar excepción
        inventarioService.validarSalidaStock(productoConStock, 5);
        // Si llega aquí sin excepción, el test pasa
    }

    // ── TU-INV-02 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-INV-02: validarSalidaStock con cantidad mayor al stock lanza ResponseStatusException")
    void movimientoSalida_mayorAlStock_lanzaExcepcion() {
        // Arrange — stock=3, solicitamos 10
        // Act + Assert
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> inventarioService.validarSalidaStock(productoSinStock, 10));

        assertThat(ex.getStatusCode().value()).isEqualTo(400);
        assertThat(ex.getReason()).contains("Stock insuficiente");
        assertThat(ex.getReason()).contains("3");  // stock actual en el mensaje
    }

    // ── TU-INV-03 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TU-INV-03: productos con stock bajo mínimo aparecen en lista de alertas")
    void productoConStockBajoMinimo_apareceEnAlertas() {
        // Arrange — lista con 1 bajo mínimo, 1 normal
        when(productosRepo.findAll()).thenReturn(
            List.of(productoBajoMinimo, productoNormal)
        );

        // Act
        List<Productos> alertas = inventarioService.obtenerProductosBajoMinimo();

        // Assert
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).getNombreProducto()).isEqualTo("Pastillas de freno");
    }
}
