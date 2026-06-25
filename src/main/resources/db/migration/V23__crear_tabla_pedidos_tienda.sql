CREATE TABLE pedido_tienda (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_pedido   VARCHAR(60) NOT NULL UNIQUE,
    fecha           TIMESTAMP   NOT NULL DEFAULT now(),
    cliente_nombre  VARCHAR(200) NOT NULL,
    cliente_email   VARCHAR(200) NOT NULL,
    cliente_telefono VARCHAR(50),
    cliente_direccion TEXT,
    cliente_ciudad  VARCHAR(100),
    cliente_region  VARCHAR(100),
    notas           TEXT,
    subtotal        DECIMAL(12,2) NOT NULL,
    costo_envio     DECIMAL(12,2) NOT NULL DEFAULT 0,
    total           DECIMAL(12,2) NOT NULL,
    usuario_id      UUID REFERENCES usuario(id) ON DELETE SET NULL
);

CREATE TABLE pedido_tienda_item (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id        UUID        NOT NULL REFERENCES pedido_tienda(id) ON DELETE CASCADE,
    producto_id      UUID        REFERENCES producto(id) ON DELETE SET NULL,
    nombre_producto  VARCHAR(200) NOT NULL,
    cantidad         INT         NOT NULL,
    precio_unitario  DECIMAL(12,2) NOT NULL,
    subtotal         DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_pedido_tienda_fecha   ON pedido_tienda(fecha DESC);
CREATE INDEX idx_pedido_tienda_usuario ON pedido_tienda(usuario_id);
CREATE INDEX idx_pedido_item_pedido    ON pedido_tienda_item(pedido_id);