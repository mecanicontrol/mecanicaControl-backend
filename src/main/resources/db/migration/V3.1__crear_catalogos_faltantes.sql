-- ============================================================
-- V3.1: Crea tablas de catálogos de V1 que no fueron ejecutadas
-- (V1 fue baselined en el primer deploy, nunca ejecutada)
-- ============================================================

CREATE TABLE IF NOT EXISTS rol (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(30)  NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO rol (nombre, descripcion) VALUES
('ADMIN',    'Administrador con acceso total al sistema'),
('TECNICO',  'Mecánico del taller, gestiona sus órdenes de trabajo'),
('VENDEDOR', 'Gestiona inventario y movimientos de stock'),
('CLIENTE',  'Cliente registrado del taller')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS nivel_tecnico (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(20) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO nivel_tecnico (nombre, descripcion) VALUES
('JUNIOR', 'Técnico en formación, hasta 2 años de experiencia'),
('SENIOR', 'Técnico experimentado, entre 2 y 5 años'),
('MASTER', 'Técnico experto, más de 5 años de experiencia')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS especialidad (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO especialidad (nombre, descripcion) VALUES
('Motor',      'Diagnóstico y reparación de motores'),
('Eléctrico',  'Sistema eléctrico, batería y alternador'),
('Frenos',     'Sistema de frenos, discos y pastillas'),
('Suspensión', 'Amortiguadores, resortes y dirección'),
('Carrocería', 'Soldadura, pintura y reparación de carrocería'),
('Neumáticos', 'Montaje, desmontaje, alineación y balanceo'),
('General',    'Mantención preventiva general')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS marca_vehiculo (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(80) NOT NULL UNIQUE
);
INSERT INTO marca_vehiculo (nombre) VALUES
('Toyota'),('Chevrolet'),('Hyundai'),('Kia'),('Suzuki'),
('Nissan'),('Ford'),('Volkswagen'),('Honda'),('Mazda'),
('Mitsubishi'),('Subaru'),('Mercedes-Benz'),('BMW'),('Audi'),
('Peugeot'),('Renault'),('Citroën'),('Fiat'),('Jeep'),('Otro')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS modelo_vehiculo (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    marca_vehiculo_id UUID        NOT NULL REFERENCES marca_vehiculo(id),
    nombre            VARCHAR(80) NOT NULL,
    UNIQUE (marca_vehiculo_id, nombre)
);

CREATE TABLE IF NOT EXISTS tipo_combustible (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(30) NOT NULL UNIQUE
);
INSERT INTO tipo_combustible (nombre) VALUES
('Gasolina'),('Diésel'),('Eléctrico'),('Híbrido'),('GLP'),('GNV')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS categoria_servicio (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO categoria_servicio (nombre, descripcion) VALUES
('Mantención',  'Servicios de mantención preventiva periódica'),
('Diagnóstico', 'Diagnóstico computarizado y evaluación técnica'),
('Frenos',      'Reparación y mantención del sistema de frenos'),
('Suspensión',  'Reparación de suspensión y dirección'),
('Motor',       'Reparación y overhaul de motor'),
('Eléctrico',   'Sistema eléctrico, batería y electrónica'),
('Carrocería',  'Soldadura, reparación y pintura de carrocería'),
('Neumáticos',  'Montaje, balanceo y alineación de neumáticos')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS categoria_producto (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO categoria_producto (nombre, descripcion) VALUES
('Lubricantes',   'Aceites y grasas lubricantes'),
('Filtros',       'Filtros de aceite, aire, combustible y habitáculo'),
('Frenos',        'Pastillas, discos, zapatas y líquido de frenos'),
('Suspensión',    'Amortiguadores, resortes y rótulas'),
('Eléctrico',     'Baterías, alternadores, fusibles y cables'),
('Neumáticos',    'Neumáticos y llantas'),
('Correas',       'Correas de distribución, alternador y accesorios'),
('Refrigeración', 'Refrigerante, termostatos y mangueras'),
('Carrocería',    'Piezas de carrocería y pintura'),
('Herramientas',  'Herramientas y equipos del taller'),
('Otros',         'Repuestos y accesorios varios')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS marca_producto (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(80) NOT NULL UNIQUE
);
INSERT INTO marca_producto (nombre) VALUES
('Bosch'),('NGK'),('Castrol'),('Mobil'),('Shell'),('Gates'),
('Dayco'),('Monroe'),('Sachs'),('Brembo'),('Ferodo'),
('Mann Filter'),('Fram'),('Denso'),('Valeo'),('Genérico')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS estado_agendamiento (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO estado_agendamiento (nombre, descripcion) VALUES
('PENDIENTE',  'Agendamiento recibido, esperando confirmación del taller'),
('CONFIRMADO', 'Confirmado por el taller, técnico asignado'),
('EN_PROCESO', 'El vehículo ya está en el taller'),
('COMPLETADO', 'Trabajo finalizado y vehículo entregado'),
('CANCELADO',  'Agendamiento cancelado')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS estado_ot (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO estado_ot (nombre, descripcion) VALUES
('ACTIVA',          'OT creada, trabajo no iniciado'),
('EN_PROCESO',      'Vehículo en diagnóstico o reparación'),
('CONTROL_CALIDAD', 'Trabajo terminado, en revisión de calidad'),
('LISTA_ENTREGA',   'Vehículo listo para ser retirado por el cliente'),
('COMPLETADA',      'Vehículo entregado y pago registrado'),
('CANCELADA',       'OT cancelada antes de completar el trabajo')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS fase (
    id          UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    orden       SMALLINT NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO fase (nombre, orden, descripcion) VALUES
('RECEPCION',       1, 'Recepción del vehículo e inspección visual inicial'),
('DIAGNOSTICO',     2, 'Diagnóstico técnico del problema reportado'),
('EN_TRABAJO',      3, 'Ejecución de la reparación o mantención'),
('CONTROL_CALIDAD', 4, 'Verificación del trabajo realizado'),
('LISTO_ENTREGA',   5, 'Vehículo listo para entrega al cliente')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS tipo_movimiento (
    id          UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(20) NOT NULL UNIQUE,
    signo       SMALLINT NOT NULL CHECK (signo IN (1, -1)),
    descripcion VARCHAR(200)
);
INSERT INTO tipo_movimiento (nombre, signo, descripcion) VALUES
('ENTRADA',  1,  'Ingreso de productos al inventario'),
('SALIDA',  -1,  'Salida de productos por uso en OT o venta'),
('AJUSTE',   1,  'Ajuste positivo por conteo físico'),
('MERMA',   -1,  'Pérdida, daño o vencimiento de producto')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS metodo_pago (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(30) NOT NULL UNIQUE
);
INSERT INTO metodo_pago (nombre) VALUES
('EFECTIVO'),('DÉBITO'),('CRÉDITO'),('TRANSFERENCIA'),('CHEQUE')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS estado_pago (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(20) NOT NULL UNIQUE
);
INSERT INTO estado_pago (nombre) VALUES
('PENDIENTE'),('PARCIAL'),('COMPLETO'),('ANULADO')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS canal_notificacion (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(20) NOT NULL UNIQUE
);
INSERT INTO canal_notificacion (nombre) VALUES
('EMAIL'),('WHATSAPP'),('SISTEMA')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS tipo_notificacion (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO tipo_notificacion (nombre, descripcion) VALUES
('AGENDAMIENTO_CONFIRMADO', 'Confirmación de agendamiento al cliente'),
('VEHICULO_LISTO',          'Vehículo listo para retirar'),
('OT_AVANCE_FASE',          'Avance de fase en la OT'),
('STOCK_CRITICO',           'Alerta de stock bajo el mínimo'),
('PAGO_REGISTRADO',         'Confirmación de pago registrado'),
('COTIZACION_ENVIADA',      'Cotización enviada al cliente'),
('RECORDATORIO_SERVICIO',   'Recordatorio de mantención próxima'),
('PREDICCION_TIEMPO',       'Estimación de tiempo de servicio por IA')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS tipo_documento (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);
INSERT INTO tipo_documento (nombre, descripcion) VALUES
('INFORME_OT',       'Informe técnico completo de la orden de trabajo'),
('COMPROBANTE_PAGO', 'Comprobante de pago emitido al cliente'),
('COTIZACION_PDF',   'Cotización generada para el cliente'),
('FOTO_VEHICULO',    'Fotografía del vehículo en recepción o proceso'),
('FOTO_PERFIL',      'Foto de perfil de usuario o técnico'),
('OTRO',             'Documento de otro tipo')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS tipo_entidad_documento (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO tipo_entidad_documento (nombre) VALUES
('ORDEN_TRABAJO'),('AGENDAMIENTO'),('PAGO'),('COTIZACION'),('USUARIO')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS condicion_pago (
    id     UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO condicion_pago (nombre) VALUES
('Contado'),('15 días'),('30 días'),('60 días'),('90 días'),('Consignación')
ON CONFLICT (nombre) DO NOTHING;

CREATE TABLE IF NOT EXISTS nivel_fidelizacion (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        VARCHAR(30)   NOT NULL UNIQUE,
    puntos_min    INT           NOT NULL CHECK (puntos_min >= 0),
    puntos_max    INT,
    descuento_pct DECIMAL(5,2)  NOT NULL DEFAULT 0.00
                  CHECK (descuento_pct >= 0 AND descuento_pct <= 100),
    descripcion   VARCHAR(200)
);
INSERT INTO nivel_fidelizacion (nombre, puntos_min, puntos_max, descuento_pct, descripcion) VALUES
('BASICO',   0,     999,   0.00, 'Nivel de entrada, sin descuento adicional'),
('SILVER',   1000,  4999,  5.00, 'Descuento del 5% en todos los servicios'),
('GOLD',     5000,  9999,  10.00,'Descuento del 10% en todos los servicios'),
('PLATINUM', 10000, NULL,  15.00,'Descuento del 15% en todos los servicios')
ON CONFLICT (nombre) DO NOTHING;

-- Agrega rol_id a usuario si la versión anterior de V2 no la incluía
ALTER TABLE IF EXISTS usuario
    ADD COLUMN IF NOT EXISTS rol_id UUID REFERENCES rol(id);
