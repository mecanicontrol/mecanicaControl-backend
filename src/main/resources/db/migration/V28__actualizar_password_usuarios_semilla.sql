-- Actualiza el password_hash de los usuarios semilla con hashes BCrypt reales.
-- Password de todos los usuarios de demo: Admin2026!
UPDATE usuario SET password_hash = '$2a$12$/sCch5MXr499LMujWYvbkuypF8VPDqPZJoEmWr5KSo6ATVilZe1zW'
WHERE email IN (
    'admin@mecanicahub.cl',
    'tecnico1@mecanicahub.cl',
    'vendedor1@mecanicahub.cl',
    'cliente1@gmail.com'
);