-- ============================================================
-- V15.1: Normaliza la columna de rol en usuario
-- La V2 original tenía 'rol' VARCHAR NOT NULL en lugar de 'rol_id' UUID FK
-- La tabla usuario no tiene datos aún (V16 es el primer INSERT)
-- ============================================================

DO $$
BEGIN
    -- Eliminar rol_id UUID nullable que agregó V3.1 (si existe)
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'usuario' AND column_name = 'rol_id') THEN
        ALTER TABLE usuario DROP COLUMN rol_id;
    END IF;

    -- Eliminar la columna 'rol' VARCHAR que vino de la V2 original (si existe)
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'usuario' AND column_name = 'rol') THEN
        ALTER TABLE usuario DROP COLUMN rol;
    END IF;
END $$;

-- Agregar la columna correcta: UUID NOT NULL con FK a rol
-- La tabla usuario está vacía así que NOT NULL es válido sin DEFAULT
ALTER TABLE usuario ADD COLUMN rol_id UUID NOT NULL REFERENCES rol(id);
CREATE INDEX idx_usuario_rol_id ON usuario(rol_id);
