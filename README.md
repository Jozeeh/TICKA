# TICKA — Sistema de Eventos en Vivo

Sistema web para administrar espectáculos en vivo (conciertos, partidos, ópera y teatro) con venta de boletos **por asiento**: recintos con zonas y asientos numerados, eventos con precios por zona, mapa de asientos, control de acceso en puerta y reportes para la administración.

Proyecto integrador del módulo **Desarrollo de Aplicaciones Multiplataforma (COIDS3504)** — ITCA-FEPADE, grupo IDSN421, Equipo 11.

- Requerimientos: [`docs/requerimientos.md`](docs/requerimientos.md)
- Modelo de datos: [`docs/diagrama_er.md`](docs/diagrama_er.md)
- Guion de la demostración: [`docs/guion_demo.md`](docs/guion_demo.md)

## Estado del avance

| Rol | Módulos | Estado |
|---|---|---|
| **Administrador** | M6 Usuarios y seguridad · M7 Dashboard y reportes | ✅ Implementado |
| **Organizador** | M1 Eventos y cartelera · M2 Recintos y asientos | ✅ Implementado |
| Cliente | M3 Portal del cliente (cartelera, compra, mis boletos) | Pendiente (siguiente entrega) |
| Control de acceso | M4 Validación en puerta · M5 Incidencias | Pendiente (siguiente entrega) |

La base de datos está completa para los 4 roles, y los datos de prueba incluyen ventas, accesos e incidencias para que los reportes del Administrador muestren información real.

## Tecnologías

Java 11 · Maven (WAR) · Apache Tomcat 9 (`javax.servlet.*`, nunca `jakarta`) · Servlets + JSP/JSTL · JDBC con patrón DAO · MySQL 8 · BCrypt (jBCrypt) · JUnit 4.

## Puesta en marcha

### 1. Base de datos (MySQL 8, con root)

```bash
mysql -u root -p < sql/01_esquema_ticka.sql
mysql -u root -p < sql/02_datos_prueba.sql
```

Luego abra `sql/00_usuario_aplicacion.sql`, cambie `CAMBIAR_LOCALMENTE` por una contraseña propia y ejecútelo. Crea el usuario `ticka_app`, con permisos solo de lectura y escritura sobre `ticka`; la aplicación nunca se conecta como root.

> `01_esquema_ticka.sql` borra y vuelve a crear la base `ticka`. Ejecútelo solo cuando quiera empezar de cero.

### 2. Credenciales locales (como un `.env`, no se suben a Git)

Copie `src/main/resources/db.properties.example` como `src/main/resources/db.properties` (en la misma carpeta) y escriba su usuario y contraseña de MySQL. Ese archivo está en `.gitignore`. Después de cambiarlo, use **Clean and Build**.

`ConexionBD` busca la configuración en este orden:
1. Variables de entorno `TICKA_DB_URL`, `TICKA_DB_USER` y `TICKA_DB_PASSWORD`, para un servidor de producción.
2. `src/main/resources/db.properties`, el archivo local del proyecto.
3. `%USERPROFILE%\.ticka\db.properties`, como respaldo.

### 3. Ejecutar

- **NetBeans:** abra la carpeta del proyecto. En *Propiedades → Run*, elija el servidor **Apache Tomcat 9** (no Tomcat 10/11, que usan `jakarta`). Después use *Run*.
- **Maven:** ejecute `mvn clean package` y copie `target/ticka.war` a la carpeta `webapps` de Tomcat 9.

Abra <http://localhost:8080/ticka/>: la página de inicio muestra la cartelera de eventos leída de MySQL; si aparece un aviso de conexión, revise MySQL y `db.properties`.

### 4. Primer acceso

La primera vez, `/acceso` lleva a **Configuración inicial** para crear el administrador (el repositorio no guarda usuarios ni contraseñas). Desde **Usuarios** el administrador crea después las cuentas del personal, por ejemplo un Organizador.

Las cuentas de `02_datos_prueba.sql` (clientes y personal de demostración) **no pueden iniciar sesión**: existen solo para que haya ventas y accesos en los reportes.

## Arquitectura

`JSP (formulario) → Filtros → Servlet → Servicio → DAO → MySQL → JSP (resultado)`

| Capa | Paquete / carpeta | Responsabilidad |
|---|---|---|
| Modelo | `modelo` | Entidades (`Evento`, `Recinto`, `Zona`, `Asiento`, `Usuario`...) y enums (`Rol`, `EstadoEvento`, `TipoEvento`) |
| DAO | `dao`, `dao.impl` | Todo el SQL, siempre con `PreparedStatement` |
| Servicio | `servicio` | Reglas de negocio, validaciones y transacciones (`commit`/`rollback`) |
| Controlador | `controlador`, `controlador.organizador`, `controlador.admin` | Leer la petición, delegar y elegir la vista |
| Filtros | `filtro` | UTF-8, protección CSRF, sesión y permisos por rol |
| Persistencia | `persistencia.ConexionBD` | Conexión JDBC con credenciales externas |
| Vistas | `WEB-INF/vistas/{plantilla,acceso,panel,organizador,admin,error}` | JSP + JSTL, sin SQL |

## Funcionalidad por rol

### Organizador (`/organizador/*`; el Administrador también tiene acceso)

| Pantalla | Ruta | Requerimientos |
|---|---|---|
| Eventos: listado con búsqueda, filtros y paginación | `/organizador/eventos` | RF-01, RF-03 |
| Nuevo / editar evento (participantes, fechas, recinto) | `?accion=nuevo`, `?accion=editar&id=` | RF-01, RF-06, RN-05, RN-06 |
| Detalle: precios por zona, ocupación, publicar, cambiar estado, cancelar | `?accion=ver&id=` | RF-02, RF-05, RF-07, RN-09 |
| Mapa de asientos con bloqueo y desbloqueo | `/organizador/mapa?evento=` | RF-08, RN-01, RN-11 |
| Recintos | `/organizador/recintos` | RF-10 |
| Zonas, generación de asientos e inhabilitación | `/organizador/zonas?recinto=` | RF-11, RF-12, RN-14 |
| Categorías | `/organizador/categorias` | RF-04 |

### Administrador (`/admin/*`)

| Pantalla | Ruta | Requerimientos |
|---|---|---|
| Dashboard de indicadores | `/admin/dashboard` | RF-43 |
| Usuarios del personal: crear, editar rol y estado, restablecer contraseña, desbloquear | `/admin/usuarios` | RF-38, RF-40, RF-41 |
| Clientes: consulta y activación | `/admin/clientes` | RF-38 |
| Reportes de ventas, asistencia, ocupación e incidencias, con exportación CSV | `/admin/reportes` | RF-44 a RF-49 |

### Común

Inicio y cierre de sesión con cookie "recordar usuario" (RF-37, RF-42), bloqueo de 15 minutos tras 5 intentos fallidos (RF-41), expiración de la sesión a los 20 minutos de inactividad, páginas de error 403/404/500 y página de inicio pública con la cartelera de eventos.

## Seguridad

- Contraseñas con **BCrypt** (hash con salt). Nunca se guardan ni se muestran en texto plano.
- **Filtro de autorización:** `/admin/*` solo admite Administrador; `/organizador/*` admite Organizador y Administrador; cualquier otro rol recibe 403.
- **Token CSRF** en todos los formularios POST, y cierre de sesión solo por POST.
- Consultas parametrizadas (sin concatenar datos del usuario) y salida escapada con `c:out` (XSS).
- Se descarta la sesión anónima al iniciar sesión (contra fijación de sesión), y la cookie de sesión es `HttpOnly`.
- Credenciales de MySQL en un archivo local tipo `.env` (`src/main/resources/db.properties`, en `.gitignore`).

## Reglas de negocio destacadas

- **Un asiento = un boleto (RN-01):** `evento_asiento` tiene `UNIQUE (id_evento, id_asiento)`.
- **Publicar (RF-07):** en una sola transacción se verifica que todas las zonas tengan precio y que no haya choque de horario, se copian los asientos activos del recinto al evento y el evento pasa a *Publicado*.
- **Choque de horario (RN-05):** un recinto no puede tener dos eventos activos traslapados.
- **Cancelar (RN-09):** en una transacción se anulan las ventas y los boletos, se liberan los asientos y el evento pasa a *Cancelado*.
- **Agotado (RN-11):** se recalcula al bloquear o desbloquear asientos.

## Pruebas automatizadas

```bash
mvn test
```

Son 28 pruebas JUnit, sin base de datos:

| Clase de prueba | Qué cubre |
|---|---|
| `ValidadorUsuarioTest` | Formato de usuario y política de contraseñas |
| `SeguridadClaveTest` | Hash BCrypt y verificación |
| `AutenticacionServicioTest` | Inicio de sesión, cuenta desactivada y bloqueo al quinto intento (DAO en memoria) |
| `AutenticacionFilterTest` | Matriz de permisos por rol |
| `ValidadorEventoTest` | Fechas del evento y regla de traslape de horarios |
| `GeneradorAsientosTest` | Cálculo de filas y límites; flujo de estados del evento |
| `ExportadorCsvTest` | Formato CSV, comillas y protección contra fórmulas |

## Matriz de pruebas manuales

Estas pruebas se hicieron en el entorno local con los datos de `02_datos_prueba.sql`.

| N.º | Prueba | Resultado esperado | Estado |
|---|---|---|---|
| 1 | Primer acceso sin administrador | Redirige a Configuración inicial | Verificado |
| 2 | Contraseña débil al crear usuario | Mensaje de política de contraseñas | Verificado |
| 3 | Formulario sin token CSRF | 403 | Verificado |
| 4 | 5 intentos fallidos de login | Cuenta bloqueada 15 minutos; el administrador la desbloquea | Verificado |
| 5 | Organizador abre `/admin/dashboard` | 403 Acceso denegado | Verificado |
| 6 | Administrador intenta desactivarse | Rechazado | Verificado |
| 7 | Publicar evento sin precios | "Faltan precios en 3 zonas" | Verificado |
| 8 | Publicar con precios | Evento publicado y 320 asientos habilitados | Verificado |
| 9 | Evento en el mismo recinto y horario | Rechazado por choque de horario (RN-05) | Verificado |
| 10 | Bloquear un asiento vendido | Rechazado; solo se bloquean disponibles | Verificado |
| 11 | Generar asientos repetidos | Omite los existentes e informa cuántos | Verificado |
| 12 | Cancelar evento con ventas | 22 boletos anulados y asientos liberados | Verificado |
| 13 | Reporte de asistencia en CSV | Archivo con BOM UTF-8 y totales | Verificado |
| 14 | Tildes y ñ en formularios | Se guardan y muestran correctamente | Verificado |

## Control de credenciales

- `src/main/resources/db.properties` (contraseña de MySQL) es local, como un `.env`, y está en `.gitignore`. En el repositorio solo va `db.properties.example`.
- `sql/00_usuario_aplicacion.sql` solo trae un marcador (`CAMBIAR_LOCALMENTE`).
- El repositorio no contiene usuarios con contraseña: el administrador se crea en la configuración inicial.
