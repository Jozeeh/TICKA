-- =====================================================================
-- TICKA — Datos de prueba para la demostración
-- ---------------------------------------------------------------------
-- Ejecutar con root DESPUÉS de 01_esquema_ticka.sql (sobre una base recién
-- creada: los identificadores se asignan en orden desde 1).
--
-- Qué carga:
--   * 2 recintos (teatro y estadio) con 6 zonas y 468 asientos generados.
--   * 8 eventos con artistas y equipos reales pero datos ficticios:
--     5 publicados (Maná, Ricardo Arjona, El Fantasma de la Ópera, la final
--     Alianza vs. FAS y La Selecta vs. Costa Rica), 1 en borrador (Shakira,
--     para publicar en vivo), 1 finalizado con asistencia (La Traviata) y
--     1 cancelado (final de baloncesto).
--   * 8 clientes, 27 ventas con sus boletos, accesos en puerta e incidencias.
--
-- Las fechas son relativas a hoy (CURDATE), así la demo siempre tiene
-- eventos próximos y eventos pasados.
--
-- SEGURIDAD: ninguna cuenta de este script puede iniciar sesión. Su
-- password_hash es '!', que no es un hash BCrypt válido. El primer
-- administrador se crea desde la aplicación (/configuracion-inicial).
-- =====================================================================

USE ticka;

-- ---------------------------------------------------------------------
-- Tabla auxiliar de números 1..30 para generar filas y asientos.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS aux_numeros;
CREATE TABLE aux_numeros (n TINYINT UNSIGNED PRIMARY KEY);
INSERT INTO aux_numeros (n)
WITH RECURSIVE serie (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM serie WHERE n < 30)
SELECT n FROM serie;

-- ---------------------------------------------------------------------
-- Usuarios de apoyo (inactivos) y clientes. No pueden iniciar sesión.
-- id 1: vendedor de mostrador · id 2: personal de puerta · id 3..10: clientes
-- ---------------------------------------------------------------------
INSERT INTO usuario (id, username, password_hash, nombre, id_rol, activo) VALUES
    (1,  'ventas.demo',  '!', 'Taquilla de demostración', 4, 0),
    (2,  'puerta.demo',  '!', 'Puerta de demostración',   3, 0),
    (3,  'cliente01',    '!', 'Ana López',        2, 1),
    (4,  'cliente02',    '!', 'Carlos Méndez',    2, 1),
    (5,  'cliente03',    '!', 'Gabriela Ramos',   2, 1),
    (6,  'cliente04',    '!', 'Luis Hernández',   2, 1),
    (7,  'cliente05',    '!', 'María Fernández',  2, 1),
    (8,  'cliente06',    '!', 'José Martínez',    2, 1),
    (9,  'cliente07',    '!', 'Sofía Castillo',   2, 1),
    (10, 'cliente08',    '!', 'Diego Navarro',    2, 0);

INSERT INTO cliente (id, id_usuario, nombres, apellidos, documento, correo, telefono) VALUES
    (1, 3,  'Ana',      'López',     '04512378-1', 'ana.lopez@correo.test',      '7012-3456'),
    (2, 4,  'Carlos',   'Méndez',    '05218734-6', 'carlos.mendez@correo.test',  '7123-4567'),
    (3, 5,  'Gabriela', 'Ramos',     '03984521-0', 'gabriela.ramos@correo.test', '7234-5678'),
    (4, 6,  'Luis',     'Hernández', '06123498-3', 'luis.hernandez@correo.test', '7345-6789'),
    (5, 7,  'María',    'Fernández', '04876512-9', 'maria.fernandez@correo.test','7456-7890'),
    (6, 8,  'José',     'Martínez',  '05561234-7', 'jose.martinez@correo.test',  '7567-8901'),
    (7, 9,  'Sofía',    'Castillo',  '06345178-2', 'sofia.castillo@correo.test', '7678-9012'),
    (8, 10, 'Diego',    'Navarro',   '03765412-5', 'diego.navarro@correo.test',  '7789-0123');

-- ---------------------------------------------------------------------
-- Recintos, zonas y asientos
-- ---------------------------------------------------------------------
INSERT INTO recinto (id, nombre, direccion, tipo) VALUES
    (1, 'Teatro Presidente', 'Col. San Benito, San Salvador', 'Teatro'),
    (2, 'Estadio Cuscatlán', 'Calle Antigua a Huizúcar, San Salvador', 'Estadio');

INSERT INTO zona (id, id_recinto, nombre, descripcion) VALUES
    (1, 1, 'Platea',      'Planta baja frente al escenario'),
    (2, 1, 'Balcón',      'Segundo nivel'),
    (3, 1, 'Palco',       'Palcos laterales'),
    (4, 2, 'Sol General', 'Graderío general'),
    (5, 2, 'Sombra',      'Graderío techado'),
    (6, 2, 'Tribuna VIP', 'Tribuna central con mejor visibilidad');

-- Filas con letra (A, B, C...) y asientos numerados por fila.
INSERT INTO asiento (id_zona, fila, numero)
SELECT g.id_zona, CHAR(64 + f.n), c.n
FROM (SELECT 1 AS id_zona, 8 AS filas, 12 AS por_fila
      UNION ALL SELECT 2, 4, 10
      UNION ALL SELECT 3, 2, 6
      UNION ALL SELECT 4, 10, 20
      UNION ALL SELECT 5, 6, 15
      UNION ALL SELECT 6, 3, 10) g
JOIN aux_numeros f ON f.n <= g.filas
JOIN aux_numeros c ON c.n <= g.por_fila
ORDER BY g.id_zona, f.n, c.n;

-- Un asiento inhabilitado (RN-14) para mostrar el caso en la vista de zonas.
UPDATE asiento SET activo = 0 WHERE id_zona = 2 AND fila = 'D' AND numero = 10;

-- ---------------------------------------------------------------------
-- Eventos (categorías del script 01: 1 Rock, 2 Pop, 4 Ópera, 5 Fútbol,
-- 6 Básquetbol, 7 Teatro). Artistas y equipos reales, pero los eventos,
-- fechas, precios y ventas son ficticios: solo sirven para la demostración.
-- ---------------------------------------------------------------------
INSERT INTO evento (id, nombre, tipo, descripcion, participantes, id_categoria, id_recinto,
                    apertura_puertas, fecha_inicio, fecha_fin, estado) VALUES
    (1, 'Ricardo Arjona: Noche Acústica', 'Concierto',
        'Un recorrido íntimo por sus grandes éxitos en formato acústico, con banda reducida.',
        'Ricardo Arjona', 2, 1,
        TIMESTAMP(CURDATE() + INTERVAL 21 DAY, '18:30:00'),
        TIMESTAMP(CURDATE() + INTERVAL 21 DAY, '20:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 21 DAY, '23:00:00'), 'Publicado'),
    (2, 'Gran Final Apertura 2026', 'Partido',
        'Partido de vuelta de la gran final del torneo Apertura de la Primera División.',
        'Alianza FC vs. CD FAS', 5, 2,
        TIMESTAMP(CURDATE() + INTERVAL 35 DAY, '13:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 35 DAY, '15:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 35 DAY, '17:30:00'), 'Publicado'),
    (3, 'La Traviata', 'Opera',
        'Ópera en tres actos de Giuseppe Verdi, con puesta en escena clásica.',
        'Orquesta Sinfónica de El Salvador y Coro Nacional', 4, 1,
        TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '18:00:00'),
        TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '19:00:00'),
        TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '22:00:00'), 'Finalizado'),
    (4, 'Shakira en San Salvador', 'Concierto',
        'Show completo con sus éxitos de siempre y su producción más reciente.',
        'Shakira', 2, 2,
        TIMESTAMP(CURDATE() + INTERVAL 60 DAY, '15:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 60 DAY, '19:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 60 DAY, '22:30:00'), 'Borrador'),
    (5, 'Final Liga Superior de Baloncesto', 'Partido',
        'Final cancelada por mantenimiento del recinto.',
        'Santa Tecla BC vs. Brujos de Izalco', 6, 2,
        TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '17:00:00'),
        TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '18:00:00'),
        TIMESTAMP(CURDATE() - INTERVAL 30 DAY, '20:00:00'), 'Cancelado'),
    (6, 'Maná: Gira Centroamérica 2026', 'Concierto',
        'La banda mexicana regresa a El Salvador con un concierto de más de dos horas.',
        'Maná', 1, 2,
        TIMESTAMP(CURDATE() + INTERVAL 50 DAY, '16:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 50 DAY, '19:30:00'),
        TIMESTAMP(CURDATE() + INTERVAL 50 DAY, '22:30:00'), 'Publicado'),
    (7, 'El Fantasma de la Ópera', 'Teatro',
        'Musical clásico de Andrew Lloyd Webber en versión en español.',
        'Compañía Nacional de Teatro Musical', 7, 1,
        TIMESTAMP(CURDATE() + INTERVAL 45 DAY, '18:15:00'),
        TIMESTAMP(CURDATE() + INTERVAL 45 DAY, '19:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 45 DAY, '21:45:00'), 'Publicado'),
    (8, 'La Selecta vs. Costa Rica', 'Partido',
        'Partido amistoso de preparación de la Selección Nacional.',
        'El Salvador vs. Costa Rica', 5, 2,
        TIMESTAMP(CURDATE() + INTERVAL 14 DAY, '17:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 14 DAY, '19:00:00'),
        TIMESTAMP(CURDATE() + INTERVAL 14 DAY, '21:00:00'), 'Publicado');

-- Precios por zona (el evento 4 queda sin precios a propósito: la demo
-- muestra que no se puede publicar hasta completarlos).
INSERT INTO precio_zona (id_evento, id_zona, precio) VALUES
    (1, 1, 65.00), (1, 2, 40.00), (1, 3, 110.00),
    (2, 4, 10.00), (2, 5, 18.00), (2, 6, 45.00),
    (3, 1, 40.00), (3, 2, 25.00), (3, 3, 75.00),
    (5, 4,  8.00), (5, 5, 12.00), (5, 6, 30.00),
    (6, 4, 45.00), (6, 5, 75.00), (6, 6, 150.00),
    (7, 1, 40.00), (7, 2, 25.00), (7, 3, 70.00),
    (8, 4, 15.00), (8, 5, 25.00), (8, 6, 50.00);

-- Disponibilidad de asientos de los eventos ya publicados (lo mismo que hace
-- la aplicación al publicar).
INSERT INTO evento_asiento (id_evento, id_asiento)
SELECT e.id, a.id
FROM evento e
JOIN zona z    ON z.id_recinto = e.id_recinto
JOIN asiento a ON a.id_zona = z.id AND a.activo = 1
WHERE e.estado <> 'Borrador';

-- ---------------------------------------------------------------------
-- Ventas y boletos
-- ---------------------------------------------------------------------
INSERT INTO venta (id, id_cliente, canal, id_usuario_vendedor, fecha, total, forma_pago, estado, motivo_anulacion) VALUES
    -- Evento 1: Ricardo Arjona
    (1,  1, 'Web',       NULL, NOW() - INTERVAL 6 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (2,  2, 'Web',       NULL, NOW() - INTERVAL 5 DAY, 0, 'Transferencia', 'Pagada', NULL),
    (3,  3, 'Web',       NULL, NOW() - INTERVAL 4 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (4,  4, 'Mostrador', 1,    NOW() - INTERVAL 3 DAY, 0, 'Efectivo',      'Pagada', NULL),
    (5,  5, 'Web',       NULL, NOW() - INTERVAL 2 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (6,  6, 'Web',       NULL, NOW() - INTERVAL 1 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    -- Evento 2: Gran Final Apertura
    (7,  1, 'Web',       NULL, NOW() - INTERVAL 4 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (8,  7, 'Web',       NULL, NOW() - INTERVAL 3 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (9,  8, 'Mostrador', 1,    NOW() - INTERVAL 2 DAY, 0, 'Efectivo',      'Pagada', NULL),
    (10, 2, 'Web',       NULL, NOW() - INTERVAL 1 DAY, 0, 'Transferencia', 'Pagada', NULL),
    -- Evento 3: La Traviata (finalizado)
    (11, 3, 'Web',       NULL, NOW() - INTERVAL 25 DAY, 0, 'Tarjeta',      'Pagada', NULL),
    (12, 4, 'Web',       NULL, NOW() - INTERVAL 24 DAY, 0, 'Tarjeta',      'Pagada', NULL),
    (13, 5, 'Web',       NULL, NOW() - INTERVAL 22 DAY, 0, 'Transferencia','Pagada', NULL),
    (14, 6, 'Mostrador', 1,    NOW() - INTERVAL 20 DAY, 0, 'Efectivo',     'Pagada', NULL),
    (15, 7, 'Web',       NULL, NOW() - INTERVAL 18 DAY, 0, 'Tarjeta',      'Pagada', NULL),
    (16, 8, 'Web',       NULL, NOW() - INTERVAL 15 DAY, 0, 'Tarjeta',      'Pagada', NULL),
    -- Evento 5: Final de baloncesto (cancelado: ventas anuladas)
    (17, 1, 'Web',       NULL, NOW() - INTERVAL 40 DAY, 0, 'Tarjeta',      'Anulada', 'Evento cancelado'),
    (18, 3, 'Web',       NULL, NOW() - INTERVAL 38 DAY, 0, 'Tarjeta',      'Anulada', 'Evento cancelado'),
    -- Evento 6: Maná
    (19, 2, 'Web',       NULL, NOW() - INTERVAL 9 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (20, 5, 'Web',       NULL, NOW() - INTERVAL 8 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (21, 7, 'Mostrador', 1,    NOW() - INTERVAL 6 DAY, 0, 'Efectivo',      'Pagada', NULL),
    (22, 4, 'Web',       NULL, NOW() - INTERVAL 3 DAY, 0, 'Transferencia', 'Pagada', NULL),
    (23, 6, 'Web',       NULL, NOW() - INTERVAL 1 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    -- Evento 7: El Fantasma de la Ópera
    (24, 3, 'Web',       NULL, NOW() - INTERVAL 5 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (25, 8, 'Web',       NULL, NOW() - INTERVAL 2 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    -- Evento 8: La Selecta vs. Costa Rica
    (26, 1, 'Web',       NULL, NOW() - INTERVAL 2 DAY, 0, 'Tarjeta',       'Pagada', NULL),
    (27, 5, 'Mostrador', 1,    NOW() - INTERVAL 1 DAY, 0, 'Efectivo',      'Pagada', NULL);

-- Qué asientos compró cada venta: (venta, evento, zona, fila, desde, hasta)
DROP TABLE IF EXISTS aux_venta;
CREATE TABLE aux_venta (
    id_venta INT UNSIGNED, id_evento INT UNSIGNED, id_zona INT UNSIGNED,
    fila VARCHAR(5), desde SMALLINT UNSIGNED, hasta SMALLINT UNSIGNED
);
INSERT INTO aux_venta VALUES
    (1, 1, 1, 'A', 1, 4),   (2, 1, 1, 'A', 5, 8),   (3, 1, 1, 'B', 3, 6),
    (4, 1, 2, 'A', 1, 6),   (5, 1, 3, 'A', 1, 2),   (6, 1, 1, 'C', 1, 6),
    (7, 2, 6, 'A', 1, 4),   (8, 2, 5, 'B', 1, 6),   (9, 2, 4, 'A', 1, 6),
    (10, 2, 4, 'C', 10, 15),
    (11, 3, 1, 'A', 1, 6),  (12, 3, 1, 'B', 1, 6),  (13, 3, 1, 'C', 1, 6),
    (14, 3, 2, 'A', 1, 5),  (15, 3, 3, 'A', 1, 4),  (16, 3, 1, 'D', 1, 6),
    (17, 5, 4, 'A', 1, 4),  (18, 5, 6, 'A', 1, 2),
    (19, 6, 6, 'A', 1, 4),  (20, 6, 5, 'A', 1, 6),  (21, 6, 4, 'B', 1, 8),
    (22, 6, 6, 'B', 1, 2),  (23, 6, 4, 'D', 5, 10),
    (24, 7, 1, 'A', 3, 6),  (25, 7, 3, 'A', 1, 2),
    (26, 8, 4, 'A', 1, 8),  (27, 8, 5, 'C', 1, 4);

-- Un boleto por asiento, con código único (contenido del QR).
INSERT INTO entrada (id_venta, id_evento_asiento, precio, codigo, estado)
SELECT av.id_venta, ea.id, pz.precio,
       CONCAT('TK-', UPPER(REPLACE(UUID(), '-', ''))), 'Emitida'
FROM aux_venta av
JOIN asiento a         ON a.id_zona = av.id_zona AND a.fila = av.fila
                      AND a.numero BETWEEN av.desde AND av.hasta
JOIN evento_asiento ea ON ea.id_evento = av.id_evento AND ea.id_asiento = a.id
JOIN precio_zona pz    ON pz.id_evento = av.id_evento AND pz.id_zona = av.id_zona
ORDER BY av.id_venta, a.numero;

-- Boletos de ventas anuladas quedan anulados.
UPDATE entrada en
JOIN venta v ON v.id = en.id_venta
SET en.estado = 'Anulada'
WHERE v.estado = 'Anulada';

-- Asientos de boletos vigentes pasan a Vendido (un asiento = un boleto).
UPDATE evento_asiento ea
JOIN entrada en ON en.id_evento_asiento = ea.id
SET ea.estado = 'Vendido'
WHERE en.estado <> 'Anulada';

-- Total de cada venta = suma de sus boletos.
UPDATE venta v
JOIN (SELECT id_venta, SUM(precio) AS total FROM entrada GROUP BY id_venta) t
  ON t.id_venta = v.id
SET v.total = t.total;

-- Asientos bloqueados por producción: Arjona (Platea H-11 y H-12) y
-- Maná (Tribuna VIP C-1 a C-4, consola de sonido).
UPDATE evento_asiento ea
JOIN asiento a ON a.id = ea.id_asiento
SET ea.estado = 'Bloqueado'
WHERE (ea.id_evento = 1 AND a.id_zona = 1 AND a.fila = 'H' AND a.numero IN (11, 12))
   OR (ea.id_evento = 6 AND a.id_zona = 6 AND a.fila = 'C' AND a.numero BETWEEN 1 AND 4);

-- Evento 1: dos asientos reservados por un cliente que aún no paga.
UPDATE evento_asiento ea
JOIN asiento a ON a.id = ea.id_asiento
SET ea.estado = 'Reservado', ea.reservado_hasta = NOW() + INTERVAL 2 HOUR, ea.id_cliente_reserva = 7
WHERE ea.id_evento = 1 AND a.id_zona = 1 AND a.fila = 'D' AND a.numero IN (5, 6);

-- ---------------------------------------------------------------------
-- Evento 3 (finalizado): accesos en puerta.
-- No asistieron: Platea fila D completa y Balcón A-5.
-- ---------------------------------------------------------------------
INSERT INTO acceso (id_evento, id_entrada, codigo_leido, fecha_hora, puerta, id_usuario, resultado)
SELECT 3, en.id, en.codigo,
       TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '18:05:00') + INTERVAL (en.id MOD 50) MINUTE,
       CASE WHEN a.id_zona = 1 THEN 'Puerta 1' ELSE 'Puerta 2' END,
       2, 'Valido'
FROM entrada en
JOIN evento_asiento ea ON ea.id = en.id_evento_asiento
JOIN asiento a         ON a.id = ea.id_asiento
WHERE ea.id_evento = 3
  AND NOT (a.id_zona = 1 AND a.fila = 'D')
  AND NOT (a.id_zona = 2 AND a.fila = 'A' AND a.numero = 5);

UPDATE entrada SET estado = 'Usada'
WHERE id IN (SELECT id_entrada FROM acceso WHERE resultado = 'Valido');

-- Dos intentos rechazados: un código falso y un boleto ya utilizado.
INSERT INTO acceso (id_evento, id_entrada, codigo_leido, fecha_hora, puerta, id_usuario, resultado, motivo_rechazo)
VALUES (3, NULL, 'TK-FALSO-0001', TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '18:40:00'),
        'Puerta 2', 2, 'Rechazado', 'Código inexistente');

INSERT INTO acceso (id_evento, id_entrada, codigo_leido, fecha_hora, puerta, id_usuario, resultado, motivo_rechazo)
SELECT 3, en.id, en.codigo, TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '18:55:00'),
       'Puerta 1', 2, 'Rechazado', 'Boleto ya utilizado'
FROM entrada en
WHERE en.id = (SELECT MIN(id_entrada) FROM acceso WHERE id_evento = 3 AND resultado = 'Valido');

-- ---------------------------------------------------------------------
-- Incidencias
-- ---------------------------------------------------------------------
INSERT INTO incidencia (id_evento, tipo, descripcion, gravedad, puerta, estado, observaciones, fecha_hora, id_usuario) VALUES
    (3, 'Medica', 'Asistente con mareo atendido por paramédicos.', 'Media', 'Puerta 1', 'Cerrada',
        'Se recuperó y continuó en la función.', TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '19:40:00'), 2),
    (3, 'Asiento en disputa', 'Dos asistentes reclamaron Platea B-3; se verificó el boleto.', 'Baja', 'Puerta 1', 'Cerrada',
        'El boleto válido correspondía a la primera persona.', TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '18:50:00'), 2),
    (3, 'Objeto perdido', 'Se encontró una cartera negra en Balcón.', 'Baja', NULL, 'Abierta',
        NULL, TIMESTAMP(CURDATE() - INTERVAL 10 DAY, '22:15:00'), 2);

-- ---------------------------------------------------------------------
-- Limpieza de tablas auxiliares
-- ---------------------------------------------------------------------
DROP TABLE aux_venta;
DROP TABLE aux_numeros;

-- ---------------------------------------------------------------------
-- Verificación rápida
-- ---------------------------------------------------------------------
SELECT 'asientos' AS dato, COUNT(*) AS total FROM asiento
UNION ALL SELECT 'eventos', COUNT(*) FROM evento
UNION ALL SELECT 'ventas', COUNT(*) FROM venta
UNION ALL SELECT 'boletos vigentes', COUNT(*) FROM entrada WHERE estado <> 'Anulada'
UNION ALL SELECT 'accesos', COUNT(*) FROM acceso
UNION ALL SELECT 'incidencias', COUNT(*) FROM incidencia;
