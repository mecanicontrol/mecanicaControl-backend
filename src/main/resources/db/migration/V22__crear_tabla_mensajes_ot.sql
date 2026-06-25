CREATE TABLE mensajes_ot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_trabajo_id UUID NOT NULL REFERENCES orden_trabajo(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuario(id),
    rol VARCHAR(20) NOT NULL,
    contenido TEXT NOT NULL,
    creado_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_mensajes_ot_ot_id ON mensajes_ot(orden_trabajo_id);