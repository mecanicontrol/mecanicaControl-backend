-- ============================================================
-- V9: AGENDAMIENTO Y COTIZACION
-- agendamiento → estado_agendamiento (V1)
-- ============================================================

CREATE TABLE agendamiento (
    id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    vehiculo_id            UUID          NOT NULL REFERENCES vehiculo(id),
    tecnico_id             UUID          REFERENCES tecnico(id) ON DELETE SET NULL,
    servicio_id            UUID          NOT NULL REFERENCES servicio_catalogo(id),
    estado_agendamiento_id UUID          NOT NULL REFERENCES estado_agendamiento(id),
    fecha_hora_inicio      TIMESTAMP     NOT NULL,
    fecha_hora_fin         TIMESTAMP,
    notas_cliente          TEXT,
    notas_internas         TEXT,
    precio_acordado        DECIMAL(12,2) CHECK (precio_acordado >= 0),
    created_at             TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_agendamiento_fechas
        CHECK (fecha_hora_fin IS NULL OR fecha_hora_fin > fecha_hora_inicio)
);
CREATE INDEX idx_agendamiento_vehiculo_id  ON agendamiento(vehiculo_id);
CREATE INDEX idx_agendamiento_tecnico_id   ON agendamiento(tecnico_id);
CREATE INDEX idx_agendamiento_servicio_id  ON agendamiento(servicio_id);
CREATE INDEX idx_agendamiento_estado_id    ON agendamiento(estado_agendamiento_id);
CREATE INDEX idx_agendamiento_fecha        ON agendamiento(fecha_hora_inicio);

-- ──────────────────────────────────────────────────────────────

CREATE TABLE cotizacion (
    id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id         UUID          REFERENCES cliente(id) ON DELETE SET NULL,
    servicio_id        UUID          NOT NULL REFERENCES servicio_catalogo(id),
    marca_vehiculo_id  UUID          NOT NULL REFERENCES marca_vehiculo(id),
    modelo_vehiculo_id UUID          NOT NULL REFERENCES modelo_vehiculo(id),
    anio               SMALLINT      NOT NULL CHECK (anio >= 1900 AND anio <= 2100),
    precio_estimado    DECIMAL(12,2) NOT NULL CHECK (precio_estimado >= 0),
    pdf_url            VARCHAR(500),
    expira_at          TIMESTAMP     NOT NULL DEFAULT (NOW() + INTERVAL '30 days'),
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cotizacion_cliente_id  ON cotizacion(cliente_id);
CREATE INDEX idx_cotizacion_servicio_id ON cotizacion(servicio_id);
CREATE INDEX idx_cotizacion_expira_at   ON cotizacion(expira_at);
