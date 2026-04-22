-- ============================================================
-- V2: USUARIO
-- rol_id → rol (V1)
-- ============================================================

CREATE TABLE usuario (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        VARCHAR(100) NOT NULL,
    apellido      VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol_id        UUID         NOT NULL REFERENCES rol(id),
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuario_email  ON usuario(email);
CREATE INDEX idx_usuario_rol_id ON usuario(rol_id);
CREATE INDEX idx_usuario_activo ON usuario(activo);
