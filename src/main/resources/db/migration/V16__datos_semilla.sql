-- ============================================================
-- V16: DATOS SEMILLA
-- IMPORTANTE: reemplazar password_hash antes de producción
-- ============================================================

-- Usuarios base
INSERT INTO usuario (id, nombre, apellido, email, password_hash, rol_id)
SELECT u.id, u.nombre, u.apellido, u.email, u.password_hash, r.id
FROM (VALUES
    ('00000000-0000-0000-0000-000000000001'::UUID, 'José',   'Gálvez',  'admin@mecanicahub.cl',     '$2a$12$PLACEHOLDER_CAMBIAR_PRODUCCION', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000002'::UUID, 'Carlos', 'Ramírez', 'tecnico1@mecanicahub.cl',  '$2a$12$PLACEHOLDER_CAMBIAR_PRODUCCION', 'TECNICO'),
    ('00000000-0000-0000-0000-000000000003'::UUID, 'Ana',    'López',   'vendedor1@mecanicahub.cl', '$2a$12$PLACEHOLDER_CAMBIAR_PRODUCCION', 'VENDEDOR'),
    ('00000000-0000-0000-0000-000000000004'::UUID, 'Pedro',  'Soto',    'cliente1@gmail.com',       '$2a$12$PLACEHOLDER_CAMBIAR_PRODUCCION', 'CLIENTE')
) AS u(id, nombre, apellido, email, password_hash, rol_nombre)
JOIN rol r ON r.nombre = u.rol_nombre;

-- Perfiles
INSERT INTO perfil_usuario (usuario_id, telefono, rut) VALUES
('00000000-0000-0000-0000-000000000001', '+56912345678', '12345678-K'),
('00000000-0000-0000-0000-000000000002', '+56923456789', '23456789-K'),
('00000000-0000-0000-0000-000000000003', '+56934567890', '34567890-K'),
('00000000-0000-0000-0000-000000000004', '+56945678901', '45678901-K');

-- Técnico (nivel Senior, especialidad Motor)
INSERT INTO tecnico (usuario_id, nivel_tecnico_id, disponible, horas_semana_max, tarifa_hora)
SELECT '00000000-0000-0000-0000-000000000002', n.id, TRUE, 44, 8500
FROM nivel_tecnico n WHERE n.nombre = 'SENIOR';

-- Especialidades del técnico
INSERT INTO tecnico_especialidad (tecnico_id, especialidad_id, es_principal)
SELECT t.id, e.id, (e.nombre = 'Motor')
FROM tecnico t
JOIN usuario u ON u.id = t.usuario_id
JOIN especialidad e ON e.nombre IN ('Motor', 'Eléctrico')
WHERE u.id = '00000000-0000-0000-0000-000000000002';

-- Disponibilidad técnico Lunes a Viernes 8:00-18:00
INSERT INTO disponibilidad_tecnico (tecnico_id, dia_semana, hora_inicio, hora_fin)
SELECT t.id, d.dia, '08:00'::TIME, '18:00'::TIME
FROM tecnico t
JOIN usuario u ON u.id = t.usuario_id
CROSS JOIN (VALUES (1),(2),(3),(4),(5)) AS d(dia)
WHERE u.id = '00000000-0000-0000-0000-000000000002';

-- Cliente (nivel Básico)
INSERT INTO cliente (usuario_id, nivel_fidelizacion_id, puntos_fidelizacion)
SELECT '00000000-0000-0000-0000-000000000004', n.id, 0
FROM nivel_fidelizacion n WHERE n.nombre = 'BASICO';

-- Proveedores
INSERT INTO proveedor (nombre, rut, contacto, email, telefono, condicion_pago_id)
SELECT p.nombre, p.rut, p.contacto, p.email, p.telefono, cp.id
FROM (VALUES
    ('Repuestos AutoParts Ltda.', '76543210-K', 'Juan Pérez',   'ventas@autoparts.cl',   '+56222345678', '30 días'),
    ('Distribuidora MotorChile',  '65432109-8', 'María Torres', 'pedidos@motorchile.cl', '+56222456789', 'Contado'),
    ('Lubricantes Premium S.A.',  '54321098-7', 'Luis Castro',  'comercial@lubprem.cl',  '+56222567890', '15 días')
) AS p(nombre, rut, contacto, email, telefono, condicion)
JOIN condicion_pago cp ON cp.nombre = p.condicion;

-- Modelos de vehículo más comunes
INSERT INTO modelo_vehiculo (marca_vehiculo_id, nombre)
SELECT m.id, mo.nombre
FROM (VALUES
    ('Toyota','Corolla'),('Toyota','Yaris'),('Toyota','Hilux'),('Toyota','RAV4'),('Toyota','Fortuner'),
    ('Hyundai','Accent'),('Hyundai','Elantra'),('Hyundai','Tucson'),('Hyundai','Santa Fe'),
    ('Chevrolet','Spark'),('Chevrolet','Sail'),('Chevrolet','Tracker'),('Chevrolet','D-Max'),
    ('Kia','Rio'),('Kia','Cerato'),('Kia','Sportage'),
    ('Nissan','Versa'),('Nissan','March'),('Nissan','Frontier'),
    ('Ford','F-150'),('Ford','Ranger'),('Ford','Escape'),
    ('Mazda','Mazda3'),('Mazda','Mazda6'),('Mazda','CX-5'),
    ('Suzuki','Swift'),('Suzuki','Vitara'),('Suzuki','Jimny'),
    ('Volkswagen','Gol'),('Volkswagen','Polo'),('Volkswagen','Amarok'),
    ('Honda','Civic'),('Honda','HR-V'),('Honda','CR-V')
) AS mo(marca, nombre)
JOIN marca_vehiculo m ON m.nombre = mo.marca;
