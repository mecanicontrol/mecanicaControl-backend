-- ============================================================
-- V6: VEHICULO
-- marca_vehiculo_id   → marca_vehiculo (V1)
-- modelo_vehiculo_id  → modelo_vehiculo (V1)
-- tipo_combustible_id → tipo_combustible (V1)
-- ============================================================

CREATE TABLE vehiculo (
    id                  UUID     PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id          UUID     NOT NULL REFERENCES cliente(id) ON DELETE CASCADE,
    marca_vehiculo_id   UUID     NOT NULL REFERENCES marca_vehiculo(id),
    modelo_vehiculo_id  UUID     NOT NULL REFERENCES modelo_vehiculo(id),
    tipo_combustible_id UUID     NOT NULL REFERENCES tipo_combustible(id),
    patente             VARCHAR(10)  NOT NULL UNIQUE,
    anio                SMALLINT NOT NULL CHECK (anio >= 1900 AND anio <= 2100),
    color               VARCHAR(50),
    kilometraje_ingreso INT      DEFAULT 0 CHECK (kilometraje_ingreso >= 0),
    numero_motor        VARCHAR(50),
    numero_chasis       VARCHAR(50)
);
CREATE INDEX idx_vehiculo_cliente_id       ON vehiculo(cliente_id);
CREATE INDEX idx_vehiculo_patente          ON vehiculo(patente);
CREATE INDEX idx_vehiculo_marca_id         ON vehiculo(marca_vehiculo_id);
CREATE INDEX idx_vehiculo_modelo_id        ON vehiculo(modelo_vehiculo_id);
CREATE INDEX idx_vehiculo_combustible_id   ON vehiculo(tipo_combustible_id);
