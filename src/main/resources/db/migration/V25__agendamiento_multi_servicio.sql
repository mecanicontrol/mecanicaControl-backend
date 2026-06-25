-- Join table: un agendamiento puede tener múltiples servicios
CREATE TABLE IF NOT EXISTS agendamiento_servicio (
    agendamiento_id UUID NOT NULL REFERENCES agendamiento(id) ON DELETE CASCADE,
    servicio_id     UUID NOT NULL REFERENCES servicio_catalogo(id),
    PRIMARY KEY (agendamiento_id, servicio_id)
);

-- Migrar datos existentes: cada agendamiento con servicio_id pasa a la join table
INSERT INTO agendamiento_servicio (agendamiento_id, servicio_id)
SELECT id, servicio_id
FROM agendamiento
WHERE servicio_id IS NOT NULL
ON CONFLICT DO NOTHING;