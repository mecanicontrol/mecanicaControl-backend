-- ============================================================
-- V19: AJUSTES PARA FLUJO DE REGISTRO EN AGENDAMIENTO
--
-- Problema 1: vehiculo.tipo_combustible_id era NOT NULL,
--   pero cuando un cliente se registra desde el cotizador
--   no siempre conoce el tipo de combustible. Se vuelve nullable.
--
-- Problema 2: cotizacion no tenía patente, sin ella no se puede
--   hacer match entre el auto del cotizador y el auto en BD.
--
-- Problema 3: registro desde el agendamiento necesita guardar
--   el teléfono del usuario. perfil_usuario ya existe pero
--   se agrega índice para búsqueda por usuario_id si no existe.
-- ============================================================

-- 1. Hacer nullable tipo_combustible en vehiculo
ALTER TABLE vehiculo
    ALTER COLUMN tipo_combustible_id DROP NOT NULL;

-- 2. Agregar patente a cotizacion (opcional, porque puede que
--    el cliente no la ingrese, pero si la ingresa permite el match)
ALTER TABLE cotizacion
    ADD COLUMN IF NOT EXISTS patente VARCHAR(10),
    ADD COLUMN IF NOT EXISTS kilometraje INT CHECK (kilometraje >= 0);

-- 3. Índice sobre patente en cotizacion para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_cotizacion_patente ON cotizacion(patente);

-- 4. Permitir que vehiculo se cree sin modelo_vehiculo_id
--    para casos donde el modelo no existe en el catálogo
--    (ej: modelo muy nuevo o importado no listado)
ALTER TABLE vehiculo
    ALTER COLUMN modelo_vehiculo_id DROP NOT NULL;

-- 5. Agregar columna alias/apodo al vehiculo para que el cliente
--    lo identifique fácilmente en su perfil
--    Ej: "Mi Corolla rojo" o "El de mi señora"
ALTER TABLE vehiculo
    ADD COLUMN IF NOT EXISTS alias VARCHAR(100);
