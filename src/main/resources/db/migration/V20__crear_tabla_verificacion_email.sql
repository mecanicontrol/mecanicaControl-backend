-- ============================================================
-- V20: VERIFICACION EMAIL
-- Almacena tokens de verificación para activar cuentas nuevas
-- ============================================================

CREATE TABLE verificacion_email (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID        NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token       VARCHAR(64) NOT NULL UNIQUE,
    expira_at   TIMESTAMP   NOT NULL,
    usado       BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verificacion_email_token      ON verificacion_email(token);
CREATE INDEX idx_verificacion_email_usuario_id ON verificacion_email(usuario_id);
