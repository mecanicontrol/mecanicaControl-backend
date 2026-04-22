-- ============================================================
-- V17: DIAGNOSTICO_IA
-- Guarda el historial de consultas del cotizador con IA.
-- El cliente describe sus fallos y la IA recomienda servicios.
-- Fuente: Groq API (LLaMA 3)
-- ============================================================

CREATE TABLE diagnostico_ia (
    id               UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id       UUID      REFERENCES cliente(id) ON DELETE SET NULL,
    descripcion_fallo TEXT     NOT NULL,
    marca            VARCHAR(80),
    modelo           VARCHAR(80),
    anio             SMALLINT,
    kilometraje      INT,
    respuesta_ia     TEXT      NOT NULL,
    servicios_json   TEXT      NOT NULL DEFAULT '[]',
    modelo_ia        VARCHAR(100) DEFAULT 'llama3-70b-8192',
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_diagnostico_ia_cliente_id  ON diagnostico_ia(cliente_id);
CREATE INDEX idx_diagnostico_ia_created_at  ON diagnostico_ia(created_at);
