-- =====================================================================
-- TICKA — Usuario de aplicación con privilegios limitados
-- ---------------------------------------------------------------------
-- Ejecutar con una cuenta administrativa (root) UNA sola vez por equipo.
-- La aplicación JAMÁS debe conectarse como root.
--
-- IMPORTANTE: la contraseña de este script es solo un marcador. Cámbiela
-- localmente antes de ejecutarlo, escriba la misma en
-- %USERPROFILE%\.ticka\db.properties y no la suba a Git.
-- El permiso sobre ticka.* se conserva aunque 01_esquema_ticka.sql
-- vuelva a crear la base, así que el orden de ejecución no importa.
-- =====================================================================

CREATE USER IF NOT EXISTS 'ticka_app'@'localhost'
  IDENTIFIED BY 'CAMBIAR_LOCALMENTE';

GRANT SELECT, INSERT, UPDATE, DELETE
  ON ticka.*
  TO 'ticka_app'@'localhost';

FLUSH PRIVILEGES;
