-- =====================================================================
-- TICKA — Sistema de Eventos en Vivo
-- Script de base de datos (MySQL 8.0.16+, InnoDB, utf8mb4)
-- Equipo 11 · IDSN421 · COIDS3504 Desarrollo de Aplicaciones Multiplataforma
--
-- Ejecución:  mysql -u root -p < 01_esquema_ticka.sql
-- Nota: no incluye usuarios ni contraseñas. Los usuarios de prueba se crean
--       desde la aplicación (contraseña con hash BCrypt) o en un script aparte
--       que NO se sube al repositorio.
-- =====================================================================

DROP DATABASE IF EXISTS ticka;
CREATE DATABASE ticka CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE ticka;

-- =====================================================================
-- M6 · Usuarios y seguridad
-- =====================================================================
-- Los 4 perfiles del sistema (Organizador, Cliente, Control de acceso, Administrador). Define a qué puede acceder cada usuario.
CREATE TABLE rol (
    id      TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre  VARCHAR(30)      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_rol_nombre (nombre)
) ENGINE=InnoDB;

-- Cuentas para iniciar sesión (personal y clientes): contraseña con hash, rol, intentos fallidos y bloqueo temporal.
CREATE TABLE usuario (
    id                INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    username          VARCHAR(50)      NOT NULL,
    password_hash     VARCHAR(100)     NOT NULL,               -- BCrypt
    nombre            VARCHAR(100)     NOT NULL,
    id_rol            TINYINT UNSIGNED NOT NULL,
    activo            BOOLEAN     NOT NULL DEFAULT 1,
    intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,
    bloqueado_hasta   DATETIME         NULL,
    creado_en         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuario_username (username),
    KEY ix_usuario_rol (id_rol),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id)
) ENGINE=InnoDB;

-- =====================================================================
-- M3 · Clientes (cuenta propia del público)
-- =====================================================================
-- Datos personales del comprador. Cada cliente está ligado a un usuario para poder iniciar sesión.
CREATE TABLE cliente (
    id         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_usuario INT UNSIGNED NOT NULL,
    nombres    VARCHAR(80)  NOT NULL,
    apellidos  VARCHAR(80)  NOT NULL,
    documento  VARCHAR(20)  NOT NULL,
    correo     VARCHAR(120) NOT NULL,
    telefono   VARCHAR(20)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_cliente_usuario   (id_usuario),
    UNIQUE KEY uq_cliente_documento (documento),
    UNIQUE KEY uq_cliente_correo    (correo),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

-- =====================================================================
-- M2 · Recintos, zonas y asientos
-- =====================================================================
-- Lugar físico donde se realizan los eventos (estadio, arena, teatro).
CREATE TABLE recinto (
    id        INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(120) NOT NULL,
    direccion VARCHAR(200) NULL,
    tipo      ENUM('Estadio','Arena','Teatro','Otro') NOT NULL DEFAULT 'Otro',
    activo    BOOLEAN     NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_recinto_nombre (nombre)
) ENGINE=InnoDB;

-- Secciones de un recinto (Platea, Palco, Gradas...). Agrupa asientos y es la unidad de precio.
CREATE TABLE zona (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_recinto  INT UNSIGNED NOT NULL,
    nombre      VARCHAR(60)  NOT NULL,
    descripcion VARCHAR(200) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_zona_recinto_nombre (id_recinto, nombre),
    CONSTRAINT fk_zona_recinto FOREIGN KEY (id_recinto) REFERENCES recinto (id)
) ENGINE=InnoDB;

-- Cada lugar físico (zona + fila + número). Plantilla del recinto: no cambia entre eventos.
CREATE TABLE asiento (
    id      INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_zona INT UNSIGNED NOT NULL,
    fila    VARCHAR(5)   NOT NULL,
    numero  SMALLINT UNSIGNED NOT NULL,
    activo  BOOLEAN     NOT NULL DEFAULT 1,       -- RN-14: se inhabilita, no se elimina
    PRIMARY KEY (id),
    UNIQUE KEY uq_asiento_ubicacion (id_zona, fila, numero),
    CONSTRAINT fk_asiento_zona FOREIGN KEY (id_zona) REFERENCES zona (id)
) ENGINE=InnoDB;

-- =====================================================================
-- M1 · Eventos y cartelera
-- =====================================================================
-- Catálogo para clasificar eventos (Rock, Ópera, Fútbol...). Sirve para filtrar la cartelera.
CREATE TABLE categoria (
    id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(60)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_categoria_nombre (nombre)
) ENGINE=InnoDB;

-- El espectáculo (concierto, partido, ópera, teatro): recinto, fechas, apertura de puertas y estado. Tabla central.
-- 'participantes' guarda en texto quién actúa o juega (p. ej. "Maná" o "Alianza FC vs. CD FAS").
CREATE TABLE evento (
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(150) NOT NULL,
    tipo            ENUM('Concierto','Partido','Opera','Teatro','Otro') NOT NULL,
    descripcion     TEXT         NULL,
    participantes   VARCHAR(200) NULL,                    -- artista(s), elenco o equipos
    afiche          MEDIUMBLOB   NULL,                    -- imagen adjunta (opcional, máx. 2 MB)
    afiche_tipo     VARCHAR(30)  NULL,                    -- image/jpeg, image/png, image/webp o image/gif
    id_categoria    INT UNSIGNED NULL,
    id_recinto      INT UNSIGNED NOT NULL,
    apertura_puertas DATETIME    NOT NULL,
    fecha_inicio    DATETIME     NOT NULL,
    fecha_fin       DATETIME     NOT NULL,
    estado          ENUM('Borrador','Publicado','Agotado','En curso','Finalizado','Cancelado')
                    NOT NULL DEFAULT 'Borrador',
    activo          BOOLEAN     NOT NULL DEFAULT 1,      -- RN-10: borrado lógico
    creado_por      INT UNSIGNED NULL,
    creado_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_evento_fecha   (fecha_inicio),
    KEY ix_evento_estado  (estado),
    KEY ix_evento_recinto_fechas (id_recinto, fecha_inicio, fecha_fin),   -- apoya la regla RN-05
    KEY ix_evento_categoria (id_categoria),
    CONSTRAINT fk_evento_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id),
    CONSTRAINT fk_evento_recinto   FOREIGN KEY (id_recinto)   REFERENCES recinto (id),
    CONSTRAINT fk_evento_creador   FOREIGN KEY (creado_por)   REFERENCES usuario (id),
    CONSTRAINT ck_evento_fechas CHECK (fecha_fin > fecha_inicio AND apertura_puertas <= fecha_inicio)
) ENGINE=InnoDB;

-- Precio de cada zona para un evento concreto (la misma zona puede costar distinto en cada evento).
CREATE TABLE precio_zona (
    id        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_evento INT UNSIGNED  NOT NULL,
    id_zona   INT UNSIGNED  NOT NULL,
    precio    DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_precio_evento_zona (id_evento, id_zona),
    CONSTRAINT fk_precio_evento FOREIGN KEY (id_evento) REFERENCES evento (id),
    CONSTRAINT fk_precio_zona   FOREIGN KEY (id_zona)   REFERENCES zona (id),
    CONSTRAINT ck_precio_valor CHECK (precio >= 0)
) ENGINE=InnoDB;

-- Disponibilidad de cada asiento por evento (fuente de verdad: un asiento = un boleto, RN-01)
-- Estado de cada asiento en un evento (Disponible/Reservado/Vendido/Bloqueado). Dibuja el mapa de asientos y garantiza un asiento = un boleto.
CREATE TABLE evento_asiento (
    id                 INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_evento          INT UNSIGNED NOT NULL,
    id_asiento         INT UNSIGNED NOT NULL,
    estado             ENUM('Disponible','Reservado','Vendido','Bloqueado') NOT NULL DEFAULT 'Disponible',
    reservado_hasta    DATETIME     NULL,
    id_cliente_reserva INT UNSIGNED NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_evento_asiento (id_evento, id_asiento),
    KEY ix_evento_asiento_estado (id_evento, estado),
    CONSTRAINT fk_ea_evento  FOREIGN KEY (id_evento)          REFERENCES evento (id),
    CONSTRAINT fk_ea_asiento FOREIGN KEY (id_asiento)         REFERENCES asiento (id),
    CONSTRAINT fk_ea_cliente FOREIGN KEY (id_cliente_reserva) REFERENCES cliente (id)
) ENGINE=InnoDB;

-- =====================================================================
-- M3 · Ventas y boletos
-- =====================================================================
-- Una compra completa (canal Web o Mostrador, total y forma de pago). Agrupa uno o más boletos.
CREATE TABLE venta (
    id                  INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_cliente          INT UNSIGNED  NOT NULL,
    canal               ENUM('Web','Mostrador') NOT NULL DEFAULT 'Web',
    id_usuario_vendedor INT UNSIGNED  NULL,                -- solo canal Mostrador
    fecha               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total               DECIMAL(10,2) NOT NULL,
    forma_pago          ENUM('Tarjeta','Transferencia','Efectivo') NOT NULL,   -- solo se registra, no se procesa
    estado              ENUM('Pagada','Anulada') NOT NULL DEFAULT 'Pagada',
    motivo_anulacion    VARCHAR(200)  NULL,
    PRIMARY KEY (id),
    KEY ix_venta_cliente (id_cliente),
    KEY ix_venta_fecha (fecha),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (id_cliente)          REFERENCES cliente (id),
    CONSTRAINT fk_venta_vendedor FOREIGN KEY (id_usuario_vendedor) REFERENCES usuario (id),
    CONSTRAINT ck_venta_total CHECK (total >= 0)
) ENGINE=InnoDB;

-- El boleto: un asiento de un evento con precio, código único (QR) y estado. Conserva el historial de compras y anulaciones.
CREATE TABLE entrada (
    id               INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_venta         INT UNSIGNED  NOT NULL,
    id_evento_asiento INT UNSIGNED NOT NULL,
    precio           DECIMAL(10,2) NOT NULL,
    codigo           VARCHAR(40)   NOT NULL,               -- código único / contenido del QR
    estado           ENUM('Emitida','Usada','Anulada') NOT NULL DEFAULT 'Emitida',
    PRIMARY KEY (id),
    UNIQUE KEY uq_entrada_codigo (codigo),
    KEY ix_entrada_venta (id_venta),
    KEY ix_entrada_ea (id_evento_asiento),
    CONSTRAINT fk_entrada_venta FOREIGN KEY (id_venta)          REFERENCES venta (id),
    CONSTRAINT fk_entrada_ea    FOREIGN KEY (id_evento_asiento) REFERENCES evento_asiento (id),
    CONSTRAINT ck_entrada_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

-- =====================================================================
-- M4 · Control de acceso   |   M5 · Incidencias
-- =====================================================================
-- Bitácora de cada escaneo en puerta (válido o rechazado, con motivo). Impide que un boleto entre dos veces.
CREATE TABLE acceso (
    id               INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_evento        INT UNSIGNED NOT NULL,
    id_entrada       INT UNSIGNED NULL,                    -- NULL si el código no existe
    codigo_leido     VARCHAR(40)  NOT NULL,
    fecha_hora       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    puerta           VARCHAR(20)  NOT NULL,
    id_usuario       INT UNSIGNED NOT NULL,
    resultado        ENUM('Valido','Rechazado') NOT NULL,
    motivo_rechazo   VARCHAR(100) NULL,                    -- inexistente / anulado / ya usado / otro evento
    anulado          BOOLEAN     NOT NULL DEFAULT 0,
    motivo_anulacion VARCHAR(200) NULL,
    PRIMARY KEY (id),
    KEY ix_acceso_evento (id_evento, fecha_hora),
    KEY ix_acceso_entrada (id_entrada),
    CONSTRAINT fk_acceso_evento  FOREIGN KEY (id_evento)  REFERENCES evento (id),
    CONSTRAINT fk_acceso_entrada FOREIGN KEY (id_entrada) REFERENCES entrada (id),
    CONSTRAINT fk_acceso_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

-- Problemas durante el evento (seguridad, médica, asiento en disputa...) con gravedad y seguimiento hasta su cierre.
CREATE TABLE incidencia (
    id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_evento     INT UNSIGNED NOT NULL,
    tipo          ENUM('Seguridad','Medica','Acceso denegado','Asiento en disputa','Objeto perdido','Otro') NOT NULL,
    descripcion   VARCHAR(500) NOT NULL,
    gravedad      ENUM('Baja','Media','Alta') NOT NULL DEFAULT 'Media',
    puerta        VARCHAR(20)  NULL,
    estado        ENUM('Abierta','En atencion','Cerrada') NOT NULL DEFAULT 'Abierta',
    observaciones VARCHAR(500) NULL,
    fecha_hora    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario    INT UNSIGNED NOT NULL,
    PRIMARY KEY (id),
    KEY ix_incidencia_evento (id_evento, tipo, gravedad),
    CONSTRAINT fk_incidencia_evento  FOREIGN KEY (id_evento)  REFERENCES evento (id),
    CONSTRAINT fk_incidencia_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

-- =====================================================================
-- Vista de apoyo para dashboard y reporte de ocupación (RF-42, RF-45)
-- =====================================================================
-- Calcula por evento y zona los asientos vendidos, reservados, disponibles y bloqueados.
CREATE VIEW v_ocupacion_evento_zona AS
SELECT ea.id_evento,
       z.id                                   AS id_zona,
       z.nombre                               AS zona,
       COUNT(*)                               AS total_asientos,
       SUM(ea.estado = 'Vendido')             AS vendidos,
       SUM(ea.estado = 'Reservado')           AS reservados,
       SUM(ea.estado = 'Disponible')          AS disponibles,
       SUM(ea.estado = 'Bloqueado')           AS bloqueados
FROM evento_asiento ea
JOIN asiento a ON a.id = ea.id_asiento
JOIN zona    z ON z.id = a.id_zona
GROUP BY ea.id_evento, z.id, z.nombre;

-- =====================================================================
-- Datos semilla (catálogos). Sin usuarios ni credenciales.
-- =====================================================================
INSERT INTO rol (nombre) VALUES
    ('Organizador'), ('Cliente'), ('Control de acceso'), ('Administrador');

INSERT INTO categoria (nombre) VALUES
    ('Rock'), ('Pop'), ('Música clásica'), ('Ópera'), ('Fútbol'), ('Básquetbol'), ('Teatro');

-- =====================================================================
-- Referencia para la capa DAO (no se ejecuta)
-- ---------------------------------------------------------------------
-- 1) Reservar un asiento sin colisiones (RN-01, RN-02). Si affected rows = 0,
--    el asiento ya no está disponible:
--
--   UPDATE evento_asiento
--      SET estado = 'Reservado', reservado_hasta = ?, id_cliente_reserva = ?
--    WHERE id = ?
--      AND (estado = 'Disponible'
--           OR (estado = 'Reservado' AND reservado_hasta < NOW()));
--
-- 2) Detectar choque de horario en un recinto (RN-05) al guardar un evento.
--    Si el resultado es mayor que 0, hay otro evento en el mismo recinto a esa hora:
--
--   SELECT COUNT(*) FROM evento
--    WHERE id_recinto = ?
--      AND id <> ?                                  -- excluir el actual al editar
--      AND activo = 1 AND estado <> 'Cancelado'
--      AND fecha_inicio < ? AND fecha_fin > ?;      -- (nuevo_fin, nuevo_inicio)
-- =====================================================================
