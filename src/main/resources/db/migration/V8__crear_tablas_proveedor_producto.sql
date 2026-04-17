-- ============================================================
-- V8: PROVEEDOR Y PRODUCTO
-- proveedor → condicion_pago (V1)
-- producto  → categoria_producto, marca_producto, proveedor
-- ============================================================

CREATE TABLE proveedor (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    condicion_pago_id UUID         REFERENCES condicion_pago(id),
    nombre            VARCHAR(200) NOT NULL,
    rut               VARCHAR(12),
    contacto          VARCHAR(100),
    email             VARCHAR(150),
    telefono          VARCHAR(20),
    activo            BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_proveedor_condicion_id ON proveedor(condicion_pago_id);
CREATE INDEX idx_proveedor_activo       ON proveedor(activo);

-- ──────────────────────────────────────────────────────────────

CREATE TABLE producto (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    proveedor_id          UUID          REFERENCES proveedor(id) ON DELETE SET NULL,
    categoria_producto_id UUID          NOT NULL REFERENCES categoria_producto(id),
    marca_producto_id     UUID          REFERENCES marca_producto(id),
    codigo_sku            VARCHAR(50)   NOT NULL UNIQUE,
    nombre                VARCHAR(200)  NOT NULL,
    descripcion           TEXT,
    precio_costo          DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (precio_costo >= 0),
    precio_venta          DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (precio_venta >= 0),
    stock_actual          INT           NOT NULL DEFAULT 0    CHECK (stock_actual >= 0),
    stock_minimo          INT           NOT NULL DEFAULT 0    CHECK (stock_minimo >= 0),
    ubicacion_bodega      VARCHAR(100),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_producto_proveedor_id   ON producto(proveedor_id);
CREATE INDEX idx_producto_categoria_id   ON producto(categoria_producto_id);
CREATE INDEX idx_producto_marca_id       ON producto(marca_producto_id);
CREATE INDEX idx_producto_codigo_sku     ON producto(codigo_sku);
CREATE INDEX idx_producto_activo         ON producto(activo);
CREATE INDEX idx_producto_stock          ON producto(stock_actual, stock_minimo);
