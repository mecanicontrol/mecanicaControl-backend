-- ============================================================
-- V10: ORDEN_TRABAJO
-- estado_ot_id → estado_ot (V1)
-- total es campo GENERATED automáticamente
-- Trigger genera código OT-YYYY-NNNN
-- ============================================================

CREATE SEQUENCE seq_codigo_ot START 1;

CREATE TABLE orden_trabajo (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    agendamiento_id   UUID          REFERENCES agendamiento(id) ON DELETE SET NULL,
    tecnico_id        UUID          REFERENCES tecnico(id) ON DELETE SET NULL,
    estado_ot_id      UUID          NOT NULL REFERENCES estado_ot(id),
    codigo_ot         VARCHAR(30)   NOT NULL UNIQUE,
    diagnostico       TEXT,
    trabajo_realizado TEXT,
    costo_mano_obra   DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (costo_mano_obra >= 0),
    costo_repuestos   DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (costo_repuestos >= 0),
    total             DECIMAL(12,2) GENERATED ALWAYS AS (costo_mano_obra + costo_repuestos) STORED,
    fecha_inicio      TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_cierre      TIMESTAMP,
    aprobada_at       TIMESTAMP
);
CREATE INDEX idx_ot_agendamiento_id ON orden_trabajo(agendamiento_id);
CREATE INDEX idx_ot_tecnico_id      ON orden_trabajo(tecnico_id);
CREATE INDEX idx_ot_estado_id       ON orden_trabajo(estado_ot_id);
CREATE INDEX idx_ot_codigo_ot       ON orden_trabajo(codigo_ot);
CREATE INDEX idx_ot_fecha_inicio    ON orden_trabajo(fecha_inicio);

CREATE OR REPLACE FUNCTION generar_codigo_ot()
RETURNS TRIGGER AS $$
BEGIN
    NEW.codigo_ot := 'OT-' || EXTRACT(YEAR FROM NOW())::TEXT || '-' ||
                     LPAD(nextval('seq_codigo_ot')::TEXT, 4, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_generar_codigo_ot
BEFORE INSERT ON orden_trabajo
FOR EACH ROW
WHEN (NEW.codigo_ot IS NULL OR NEW.codigo_ot = '')
EXECUTE FUNCTION generar_codigo_ot();
