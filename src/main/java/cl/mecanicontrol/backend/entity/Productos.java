package cl.mecanicontrol.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "producto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Productos {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID idProducto;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "categoria_producto_id")
    private CategoriaProducto categoriaProducto;

    @ManyToOne
    @JoinColumn(name = "marca_producto_id")
    private MarcaProducto marcaProducto;

    @Column(name ="codigo_sku")
    private String sku;

    @Column(name ="nombre")
    private String nombreProducto;

    @Column(name ="descripcion")
    private String descripcionProduct;

    @Column(name ="precio_costo", precision = 12, scale = 2)
    private BigDecimal precioCosto;

    @Column(name ="precioVenta", precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "ubicacion_bodega")
    private String ubicacionBodega;

    @Column(name = "activo")
    private Boolean productoActivo;












}
