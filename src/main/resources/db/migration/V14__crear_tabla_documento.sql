-- ============================================================
-- V14: DOCUMENTO
-- tipo_documento_id         → tipo_documento (V1)
-- tipo_entidad_documento_id → tipo_entidad_documento (V1)
-- ============================================================

CREATE TABLE documento (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_id                UUID         NOT NULL,
    tipo_entidad_documento_id UUID         NOT NULL REFERENCES tipo_entidad_documento(id),
    tipo_documento_id         UUID         NOT NULL REFERENCES tipo_documento(id),
    nombre                    VARCHAR(200) NOT NULL,
    url_storage               VARCHAR(500) NOT NULL,
    mime_type                 VARCHAR(100),
    tamano_bytes              INT          CHECK (tamano_bytes > 0),
    subido_por                UUID         REFERENCES usuario(id) ON DELETE SET NULL,
    created_at                TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_documento_entidad_id   ON documento(entidad_id);
CREATE INDEX idx_documento_tipo_entidad ON documento(tipo_entidad_documento_id);
CREATE INDEX idx_documento_tipo_doc     ON documento(tipo_documento_id);
CREATE INDEX idx_documento_subido_por   ON documento(subido_por);
