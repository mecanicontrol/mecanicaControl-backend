-- ============================================================
-- V3: PERFIL_USUARIO Y SESION_USUARIO
-- ============================================================

CREATE TABLE perfil_usuario (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    UUID         NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    telefono      VARCHAR(20),
    direccion     VARCHAR(300),
    rut           VARCHAR(12),
    foto_url      VARCHAR(500),
    preferencias  JSONB        DEFAULT '{}',
    ultima_sesion TIMESTAMP
);
CREATE INDEX idx_perfil_usuario_id ON perfil_usuario(usuario_id);

-- ──────────────────────────────────────────────────────────────

CREATE TABLE sesion_usuario (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    expira_at  TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sesion_usuario_id ON sesion_usuario(usuario_id);
CREATE INDEX idx_sesion_token_hash ON sesion_usuario(token_hash);
CREATE INDEX idx_sesion_expira_at  ON sesion_usuario(expira_at);
