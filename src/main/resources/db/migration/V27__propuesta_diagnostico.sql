-- Propuesta de diagnóstico generada por el técnico
CREATE TABLE IF NOT EXISTS propuesta_diagnostico (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_trabajo_id UUID NOT NULL REFERENCES orden_trabajo(id),
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE_ADMIN',
    -- estados: PENDIENTE_ADMIN | APROBADA_ADMIN | RECHAZADA_ADMIN | ENVIADA_CLIENTE | ACEPTADA | ACEPTADA_PARCIAL | RECHAZADA_CLIENTE
    token_cliente VARCHAR(255) UNIQUE,
    nota_tecnico TEXT,
    nota_admin TEXT,
    nota_cliente TEXT,
    acepta_disclaimer BOOLEAN DEFAULT FALSE,
    creado_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resuelto_at TIMESTAMP
);

-- Servicios propuestos (uno por fila, con justificación e imágenes)
CREATE TABLE IF NOT EXISTS propuesta_servicio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    propuesta_id UUID NOT NULL REFERENCES propuesta_diagnostico(id) ON DELETE CASCADE,
    servicio_id UUID NOT NULL REFERENCES servicio_catalogo(id),
    descripcion TEXT NOT NULL,
    imagenes TEXT NOT NULL DEFAULT '[]',
    incluido_en_original BOOLEAN NOT NULL DEFAULT FALSE,
    aceptado_por_cliente BOOLEAN -- NULL = sin respuesta, TRUE = aceptado, FALSE = rechazado
);