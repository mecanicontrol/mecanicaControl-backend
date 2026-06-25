CREATE TABLE IF NOT EXISTS configuracion (
    clave       VARCHAR(100) PRIMARY KEY,
    valor       TEXT         NOT NULL,
    descripcion TEXT
);

INSERT INTO configuracion (clave, valor, descripcion) VALUES
    ('notificaciones_bcc_admins',
     'jo.galvezc@duocuc.cl,cl.mohr@duocuc.cl,rau.alvarado@duocuc.cl',
     'Correos que reciben copia oculta (BCC) de todas las notificaciones del sistema');