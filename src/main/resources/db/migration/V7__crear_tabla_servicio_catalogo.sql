-- ============================================================
-- V7: SERVICIO_CATALOGO
-- categoria_servicio_id → categoria_servicio (V1)
-- especialidad_id       → especialidad (V1)
-- ============================================================

CREATE TABLE servicio_catalogo (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria_servicio_id UUID          NOT NULL REFERENCES categoria_servicio(id),
    especialidad_id       UUID          REFERENCES especialidad(id),
    nombre                VARCHAR(150)  NOT NULL,
    descripcion           TEXT,
    duracion_minutos      INT           NOT NULL DEFAULT 60 CHECK (duracion_minutos > 0),
    precio_base           DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (precio_base >= 0),
    activo                BOOLEAN       NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_servicio_categoria_id    ON servicio_catalogo(categoria_servicio_id);
CREATE INDEX idx_servicio_especialidad_id ON servicio_catalogo(especialidad_id);
CREATE INDEX idx_servicio_activo          ON servicio_catalogo(activo);

-- 15 servicios semilla
INSERT INTO servicio_catalogo
    (nombre, descripcion, categoria_servicio_id, especialidad_id, duracion_minutos, precio_base)
SELECT s.nombre, s.descripcion, cs.id, e.id, s.dur, s.precio
FROM (VALUES
    ('Mantención preventiva 5.000 km',  'Cambio aceite, filtro y revisión general',               'Mantención',  'General',    60,  35000),
    ('Mantención preventiva 10.000 km', 'Cambio aceite, filtros, bujías y revisión completa',     'Mantención',  'General',    90,  55000),
    ('Diagnóstico computarizado',        'Lectura de códigos de falla con escáner OBD2',          'Diagnóstico', NULL,         45,  25000),
    ('Cambio de frenos delanteros',      'Reemplazo de pastillas y revisión de discos',           'Frenos',      'Frenos',     90,  45000),
    ('Cambio de frenos traseros',        'Reemplazo de pastillas o zapatas traseras',             'Frenos',      'Frenos',     90,  40000),
    ('Alineación y balanceo',            'Ajuste de geometría y balanceo de 4 ruedas',            'Neumáticos',  'Neumáticos', 60,  30000),
    ('Cambio de neumáticos x4',          'Desmontaje y montaje de 4 neumáticos',                 'Neumáticos',  'Neumáticos', 60,  20000),
    ('Revisión sistema eléctrico',       'Diagnóstico de batería, alternador y circuitos',       'Eléctrico',   'Eléctrico',  90,  35000),
    ('Cambio de batería',                'Reemplazo de batería y revisión del sistema de carga', 'Eléctrico',   'Eléctrico',  30,  15000),
    ('Revisión de suspensión',           'Revisión de amortiguadores y brazos de suspensión',    'Suspensión',  'Suspensión', 60,  30000),
    ('Cambio de amortiguadores x2',      'Reemplazo de par de amortiguadores',                   'Suspensión',  'Suspensión', 120, 80000),
    ('Revisión sistema enfriamiento',    'Revisión de radiador, mangueras y termostato',         'Mantención',  'Motor',      60,  25000),
    ('Cambio correa de distribución',    'Reemplazo de correa de distribución y kit completo',   'Motor',       'Motor',      180, 120000),
    ('Reparación de motor',              'Diagnóstico y reparación de fallas internas',          'Motor',       'Motor',      480, 300000),
    ('Soldadura y carrocería',           'Trabajos de soldadura y reparación de carrocería',     'Carrocería',  'Carrocería', 120, 60000)
) AS s(nombre, descripcion, cat, esp, dur, precio)
JOIN categoria_servicio cs ON cs.nombre = s.cat
LEFT JOIN especialidad   e  ON e.nombre  = s.esp;
