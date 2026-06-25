-- Campos de estimación en OT
ALTER TABLE orden_trabajo ADD COLUMN IF NOT EXISTS dias_estimados_reparacion INT;
ALTER TABLE orden_trabajo ADD COLUMN IF NOT EXISTS fecha_estimada_salida TIMESTAMP;

-- Capacidad máxima del taller (configurable por admin)
INSERT INTO configuracion (clave, valor, descripcion) VALUES
    ('capacidad_maxima_taller', '15',
     'Número máximo de vehículos físicamente en el taller al mismo tiempo')
ON CONFLICT (clave) DO NOTHING;