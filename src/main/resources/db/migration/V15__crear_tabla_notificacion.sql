-- ============================================================
-- V15: NOTIFICACION
-- canal_notificacion_id → canal_notificacion (V1)
-- tipo_notificacion_id  → tipo_notificacion (V1)
-- ============================================================

CREATE TABLE notificacion (
    id                    UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id            UUID      NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    canal_notificacion_id UUID      NOT NULL REFERENCES canal_notificacion(id),
    tipo_notificacion_id  UUID      NOT NULL REFERENCES tipo_notificacion(id),
    asunto                VARCHAR(200),
    contenido             TEXT      NOT NULL,
    leida                 BOOLEAN   NOT NULL DEFAULT FALSE,
    enviada               BOOLEAN   NOT NULL DEFAULT FALSE,
    enviada_at            TIMESTAMP,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notif_usuario_id ON notificacion(usuario_id);
CREATE INDEX idx_notif_canal_id   ON notificacion(canal_notificacion_id);
CREATE INDEX idx_notif_tipo_id    ON notificacion(tipo_notificacion_id);
CREATE INDEX idx_notif_leida      ON notificacion(leida);
CREATE INDEX idx_notif_created_at ON notificacion(created_at);
