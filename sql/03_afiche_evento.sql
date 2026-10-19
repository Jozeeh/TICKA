-- =====================================================================
-- TICKA — Migración: afiche del evento como imagen adjunta
-- ---------------------------------------------------------------------
-- Solo para bases creadas ANTES de este cambio (con la columna
-- evento.imagen como texto). Si ejecuta 01_esquema_ticka.sql desde cero,
-- NO necesita este script.
--
-- Ejecutar con root:  mysql -u root -p < 03_afiche_evento.sql
-- La imagen se guarda en la base (MEDIUMBLOB) para que no se pierda al
-- redesplegar la aplicación y la vean todos los integrantes.
-- =====================================================================

USE ticka;

ALTER TABLE evento
    DROP COLUMN imagen,
    ADD COLUMN afiche      MEDIUMBLOB  NULL AFTER participantes,
    ADD COLUMN afiche_tipo VARCHAR(30) NULL AFTER afiche;
