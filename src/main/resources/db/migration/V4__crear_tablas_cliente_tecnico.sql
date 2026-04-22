-- ============================================================
-- V4: CLIENTE Y TECNICO
-- cliente → nivel_fidelizacion (V1)
-- tecnico → nivel_tecnico (V1)
-- ============================================================

CREATE TABLE cliente (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id            UUID         NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    nivel_fidelizacion_id UUID         NOT NULL REFERENCES nivel_fidelizacion(id),
    empresa               VARCHAR(200),
    descuento_default     DECIMAL(5,2) DEFAULT 0.00
                          CHECK (descuento_default >= 0 AND descuento_default <= 100),
    puntos_fidelizacion   INT          DEFAULT 0 CHECK (puntos_fidelizacion >= 0)
);
CREATE INDEX idx_cliente_usuario_id            ON cliente(usuario_id);
CREATE INDEX idx_cliente_nivel_fidelizacion_id ON cliente(nivel_fidelizacion_id);

-- ──────────────────────────────────────────────────────────────

CREATE TABLE tecnico (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id       UUID          NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    nivel_tecnico_id UUID          NOT NULL REFERENCES nivel_tecnico(id),
    disponible       BOOLEAN       NOT NULL DEFAULT TRUE,
    horas_semana_max INT           DEFAULT 44 CHECK (horas_semana_max > 0),
    tarifa_hora      DECIMAL(10,2) DEFAULT 0.00 CHECK (tarifa_hora >= 0)
);
CREATE INDEX idx_tecnico_usuario_id       ON tecnico(usuario_id);
CREATE INDEX idx_tecnico_nivel_tecnico_id ON tecnico(nivel_tecnico_id);
CREATE INDEX idx_tecnico_disponible       ON tecnico(disponible);
