-- ============================================================
-- V5: TECNICO_ESPECIALIDAD Y DISPONIBILIDAD_TECNICO
-- Un técnico puede tener múltiples especialidades (N:M)
-- La disponibilidad semanal es una entidad propia
-- ============================================================

CREATE TABLE tecnico_especialidad (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    tecnico_id      UUID    NOT NULL REFERENCES tecnico(id) ON DELETE CASCADE,
    especialidad_id UUID    NOT NULL REFERENCES especialidad(id),
    es_principal    BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (tecnico_id, especialidad_id)
);
CREATE INDEX idx_tec_esp_tecnico_id      ON tecnico_especialidad(tecnico_id);
CREATE INDEX idx_tec_esp_especialidad_id ON tecnico_especialidad(especialidad_id);

-- ──────────────────────────────────────────────────────────────

-- dia_semana: 1=Lunes ... 7=Domingo
CREATE TABLE disponibilidad_tecnico (
    id          UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    tecnico_id  UUID     NOT NULL REFERENCES tecnico(id) ON DELETE CASCADE,
    dia_semana  SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio TIME     NOT NULL,
    hora_fin    TIME     NOT NULL,
    activo      BOOLEAN  NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_disponibilidad_horas CHECK (hora_fin > hora_inicio),
    UNIQUE (tecnico_id, dia_semana)
);
CREATE INDEX idx_disp_tecnico_id ON disponibilidad_tecnico(tecnico_id);
CREATE INDEX idx_disp_dia_semana ON disponibilidad_tecnico(dia_semana);
