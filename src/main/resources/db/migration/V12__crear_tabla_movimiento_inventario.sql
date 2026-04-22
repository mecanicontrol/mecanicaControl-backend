-- ============================================================
-- V12: MOVIMIENTO_INVENTARIO
-- tipo_movimiento_id → tipo_movimiento (V1) — tiene signo +/-
-- Trigger actualiza stock usando el signo del tipo
-- ============================================================

CREATE TABLE movimiento_inventario (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    producto_id        UUID          NOT NULL REFERENCES producto(id),
    usuario_id         UUID          REFERENCES usuario(id) ON DELETE SET NULL,
    orden_trabajo_id   UUID          REFERENCES orden_trabajo(id) ON DELETE SET NULL,
    tipo_movimiento_id UUID          NOT NULL REFERENCES tipo_movimiento(id),
    cantidad           INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario    DECIMAL(12,2) DEFAULT 0.00 CHECK (precio_unitario >= 0),
    motivo             VARCHAR(300),
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mov_producto_id    ON movimiento_inventario(producto_id);
CREATE INDEX idx_mov_usuario_id     ON movimiento_inventario(usuario_id);
CREATE INDEX idx_mov_ot_id          ON movimiento_inventario(orden_trabajo_id);
CREATE INDEX idx_mov_tipo_id        ON movimiento_inventario(tipo_movimiento_id);
CREATE INDEX idx_mov_created_at     ON movimiento_inventario(created_at);

CREATE OR REPLACE FUNCTION actualizar_stock()
RETURNS TRIGGER AS $$
DECLARE v_signo SMALLINT;
BEGIN
    SELECT signo INTO v_signo FROM tipo_movimiento WHERE id = NEW.tipo_movimiento_id;
    UPDATE producto SET stock_actual = stock_actual + (v_signo * NEW.cantidad)
    WHERE id = NEW.producto_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_actualizar_stock
AFTER INSERT ON movimiento_inventario
FOR EACH ROW EXECUTE FUNCTION actualizar_stock();
