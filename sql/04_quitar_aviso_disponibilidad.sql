-- =====================================================================
-- TICKA — Migración: se elimina el aviso de disponibilidad
-- ---------------------------------------------------------------------
-- La función "avisar al cliente cuando se libere un asiento" se quitó del
-- alcance. Solo para bases creadas ANTES de este cambio; si ejecuta
-- 01_esquema_ticka.sql desde cero, NO necesita este script.
--
-- Ejecutar con root:  mysql -u root -p < 04_quitar_aviso_disponibilidad.sql
-- =====================================================================

USE ticka;

DROP TABLE IF EXISTS aviso_disponibilidad;
