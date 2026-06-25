-- V21: Aprobación de control de calidad por el administrador
-- Agrega campos de aprobación a fase_vehiculo para el flujo de revisión de CONTROL_CALIDAD

ALTER TABLE fase_vehiculo
  ADD COLUMN IF NOT EXISTS aprobacion_estado VARCHAR(20) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS aprobacion_nota   TEXT        DEFAULT NULL;

COMMENT ON COLUMN fase_vehiculo.aprobacion_estado IS 'Estado de aprobación admin: PENDIENTE, APROBADA, RECHAZADA';
COMMENT ON COLUMN fase_vehiculo.aprobacion_nota    IS 'Nota del admin al aprobar o rechazar la fase';