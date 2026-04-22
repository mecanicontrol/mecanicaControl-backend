-- ============================================================
-- V18: PREDICCION_TIEMPO_OT
-- Guarda la predicción de tiempo que hace la IA para cada OT.
-- La IA analiza el historial de orden_trabajo y fase_vehiculo
-- para predecir cuánto va a demorar el servicio.
-- Se muestra en la línea de tiempo del cliente y del técnico.
--
-- Fuente de datos históricos (sin modificar):
--   - orden_trabajo.fecha_inicio / fecha_cierre
--   - fase_vehiculo.inicio_at / fin_at
--   - servicio_catalogo.duracion_minutos (referencia base)
--   - tecnico.nivel_tecnico_id + tecnico_especialidad
-- ============================================================

CREATE TABLE prediccion_tiempo_ot (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_trabajo_id      UUID          NOT NULL REFERENCES orden_trabajo(id) ON DELETE CASCADE,
    servicio_id           UUID          NOT NULL REFERENCES servicio_catalogo(id),
    tecnico_id            UUID          REFERENCES tecnico(id) ON DELETE SET NULL,

    -- Predicción de la IA
    tiempo_estimado_min   INT           NOT NULL CHECK (tiempo_estimado_min > 0),
    hora_inicio_estimada  TIMESTAMP,
    hora_fin_estimada     TIMESTAMP,

    -- Desglose por fase (JSON con minutos estimados por fase)
    -- Ej: {"RECEPCION":10,"DIAGNOSTICO":30,"EN_TRABAJO":120,"CONTROL_CALIDAD":20,"LISTO_ENTREGA":10}
    desglose_fases_json   TEXT          DEFAULT '{}',

    -- Confianza de la predicción (0-100%)
    confianza_pct         DECIMAL(5,2)  CHECK (confianza_pct >= 0 AND confianza_pct <= 100),

    -- Factores considerados por la IA
    -- Ej: {"nivel_tecnico":"SENIOR","ots_similares":12,"promedio_historico":145,"especialidad_match":true}
    factores_json         TEXT          DEFAULT '{}',

    -- Tiempo real (se actualiza cuando se cierra la OT)
    tiempo_real_min       INT           CHECK (tiempo_real_min > 0),

    -- Diferencia entre predicción y realidad (para mejorar el modelo)
    error_minutos         INT GENERATED ALWAYS AS (
        CASE WHEN tiempo_real_min IS NOT NULL
             THEN tiempo_real_min - tiempo_estimado_min
             ELSE NULL
        END
    ) STORED,

    modelo_ia             VARCHAR(100)  DEFAULT 'llama3-70b-8192',
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pred_tiempo_ot_id      ON prediccion_tiempo_ot(orden_trabajo_id);
CREATE INDEX idx_pred_tiempo_servicio_id ON prediccion_tiempo_ot(servicio_id);
CREATE INDEX idx_pred_tiempo_tecnico_id ON prediccion_tiempo_ot(tecnico_id);
CREATE INDEX idx_pred_tiempo_created_at ON prediccion_tiempo_ot(created_at);

-- Trigger: actualiza updated_at automáticamente
CREATE OR REPLACE FUNCTION update_prediccion_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prediccion_updated_at
BEFORE UPDATE ON prediccion_tiempo_ot
FOR EACH ROW EXECUTE FUNCTION update_prediccion_updated_at();
