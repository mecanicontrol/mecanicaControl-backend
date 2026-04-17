-- ============================================================
-- V13: PAGO
-- metodo_pago_id → metodo_pago (V1)
-- estado_pago_id → estado_pago (V1)
-- ============================================================

CREATE TABLE pago (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_trabajo_id   UUID          NOT NULL REFERENCES orden_trabajo(id),
    cliente_id         UUID          REFERENCES cliente(id) ON DELETE SET NULL,
    metodo_pago_id     UUID          NOT NULL REFERENCES metodo_pago(id),
    estado_pago_id     UUID          NOT NULL REFERENCES estado_pago(id),
    monto_total        DECIMAL(12,2) NOT NULL CHECK (monto_total >= 0),
    monto_pagado       DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (monto_pagado >= 0),
    referencia_externa VARCHAR(200),
    comprobante_url    VARCHAR(500),
    fecha_pago         TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_pago_ot_id      ON pago(orden_trabajo_id);
CREATE INDEX idx_pago_cliente_id ON pago(cliente_id);
CREATE INDEX idx_pago_metodo_id  ON pago(metodo_pago_id);
CREATE INDEX idx_pago_estado_id  ON pago(estado_pago_id);
CREATE INDEX idx_pago_fecha      ON pago(fecha_pago);
