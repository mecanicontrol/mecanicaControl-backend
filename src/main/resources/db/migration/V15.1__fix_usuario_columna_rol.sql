-- ============================================================
-- V15.1: Normaliza la columna de rol en usuario
-- La V2 original fue aplicada con columna 'rol' en lugar de 'rol_id'
-- ============================================================

DO $$
BEGIN
    -- Caso: existe 'rol' y 'rol_id' (V3.1 agregó rol_id, pero rol sigue con NOT NULL)
    IF EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'usuario' AND column_name = 'rol')
    AND EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'usuario' AND column_name = 'rol_id')
    THEN
        ALTER TABLE usuario DROP COLUMN rol_id;
        ALTER TABLE usuario RENAME COLUMN rol TO rol_id;
        ALTER TABLE usuario ADD CONSTRAINT fk_usuario_rol
            FOREIGN KEY (rol_id) REFERENCES rol(id);

    -- Caso: solo existe 'rol' (sin rol_id)
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = 'usuario' AND column_name = 'rol')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = 'usuario' AND column_name = 'rol_id')
    THEN
        ALTER TABLE usuario RENAME COLUMN rol TO rol_id;
        ALTER TABLE usuario ADD CONSTRAINT fk_usuario_rol
            FOREIGN KEY (rol_id) REFERENCES rol(id);
    END IF;
END $$;
