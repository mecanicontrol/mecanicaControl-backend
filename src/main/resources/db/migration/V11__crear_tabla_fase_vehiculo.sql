-- ============================================================
-- V11: FASE_VEHICULO
-- fase_id → fase (V1) — tiene el orden de las fases
-- ============================================================

CREATE TABLE fase_vehiculo (
    id               UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_trabajo_id UUID      NOT NULL REFERENCES orden_trabajo(id) ON DELETE CASCADE,
    tecnico_id       UUID      REFERENCES tecnico(id) ON DELETE SET NULL,
    fase_id          UUID      NOT NULL REFERENCES fase(id),
    checklist_json   TEXT      DEFAULT '[]',
    observaciones    TEXT,
    inicio_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    fin_at           TIMESTAMP,
    CONSTRAINT chk_fase_fechas CHECK (fin_at IS NULL OR fin_at >= inicio_at)
);
CREATE INDEX idx_fase_ot_id      ON fase_vehiculo(orden_trabajo_id);
CREATE INDEX idx_fase_tecnico_id ON fase_vehiculo(tecnico_id);
CREATE INDEX idx_fase_fase_id    ON fase_vehiculo(fase_id);
