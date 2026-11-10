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
| **Cliente** | M3 Portal del cliente (registro, cartelera, mapa de asientos, compra con QR, mis boletos) | ✅ Implementado |
| **Control de acceso** | M4 Validación en puerta · M5 Incidencias | ✅ Implementado |

La base de datos está completa para los 4 roles, y los datos de prueba incluyen ventas, accesos e incidencias para que los reportes del Administrador muestren información real.

## Tecnologías

Java 11 · Maven (WAR) · Apache Tomcat 9 (`javax.servlet.*`, nunca `jakarta`) · Servlets + JSP/JSTL · JDBC con patrón DAO · MySQL 8 · BCrypt (jBCrypt) · OpenPDF (reportes y comprobantes en PDF) · ZXing (códigos QR) · Bootstrap 5.3, Chart.js y SweetAlert2 (copias locales) · JUnit 4.

## Puesta en marcha

### 1. Base de datos (MySQL 8, con root)

```bash
mysql -u root -p < sql/01_esquema_ticka.sql
mysql -u root -p < sql/02_datos_prueba.sql
```

Si su base `ticka` se creó antes de que existieran los afiches adjuntos (la columna `evento.imagen` era texto), ejecute también `sql/03_afiche_evento.sql`. Si además tiene la tabla `aviso_disponibilidad` (función retirada del alcance), ejecute `sql/04_quitar_aviso_disponibilidad.sql`.

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

### 4. Cuentas de prueba

`02_datos_prueba.sql` crea una cuenta por rol, todas con la contraseña **`itca1234`**:

| Usuario | Rol |
|---|---|
| `admin` | Administrador |
| `organizador` | Organizador |
| `puerta` | Control de acceso |
| `cliente` | Cliente (documento 01010101-0) |

Son solo para pruebas y la demostración; en un servidor real se deben cambiar o desactivar. También se pueden crear más clientes desde **Crear cuenta** (`/registro`) y más personal desde **Usuarios**.

Si se usa una base sin datos de prueba (solo `01_esquema_ticka.sql`), la primera vez `/acceso` lleva a **Configuración inicial** para crear el administrador.

Las demás cuentas de `02_datos_prueba.sql` (clientes y personal de demostración) **no pueden iniciar sesión**: existen solo para que haya ventas y accesos en los reportes.

### 5. Parámetros de negocio (`src/main/resources/ticka.properties`)

| Parámetro | Valor | Requerimiento |
|---|---|---|
| `reserva.minutos` | 10 minutos de reserva temporal | RF-18, RN-02 |
| `compra.maximo` | 6 asientos por compra | RF-19, RN-04 |
| `anulacion.horas` | 24 horas antes de la apertura de puertas | RF-23, RN-12 |
| `acceso.reentrada` | `false` (un boleto entra una vez) | RF-31 |
| `acceso.incidenciaPorRechazo` | `true` (cada rechazo crea una incidencia) | RF-35 |

Este archivo no tiene credenciales y sí se sube a Git. Después de cambiarlo, reinicie la aplicación.

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
| Eventos: listado con búsqueda, filtros (tipo, estado, recinto, categoría y fechas) y paginación | `/organizador/eventos` | RF-01, RF-03 |
| Nuevo / editar evento (participantes, fechas, recinto con los **precios de sus zonas en el mismo formulario** y afiche adjunto opcional: JPG, PNG, WEBP o GIF de hasta 2 MB, guardado en MySQL) | `?accion=nuevo`, `?accion=editar&id=` | RF-01, RF-06, RN-05, RN-06 |
| Detalle: precios por zona (consulta), ocupación, publicar, cambiar estado, cancelar y desactivar (borrado lógico, solo en Borrador o Cancelado) | `?accion=ver&id=` | RF-01, RF-02, RF-05, RF-07, RN-09, RN-10 |
| Duplicar evento: abre un evento nuevo con los datos, precios y afiche del original; solo se eligen las fechas | `?accion=duplicar&id=` | RF-09 |
| Mapa de asientos con bloqueo y desbloqueo | `/organizador/mapa?evento=` | RF-08, RN-01, RN-11 |
| Recintos | `/organizador/recintos` | RF-10 |
| Zonas: un solo formulario "Guardar zona" con nombre, descripción y distribución de asientos; inhabilitación de asientos sueltos con un clic | `/organizador/zonas?recinto=` | RF-11, RF-12, RN-14 |
| Categorías | `/organizador/categorias` | RF-04 |
| Calendario mensual de eventos (filtro por recinto y "mis eventos") | `/organizador/calendario` | RF-50, RF-52 |
| "Solo mis eventos" en el listado de eventos | `/organizador/eventos?mios=si` | RF-52 |
| Finalización automática de eventos y liberación de reservas vencidas (tarea cada minuto) | `AplicacionListener` + `TareaEstadosEventos` | RF-51 |

### Administrador (`/admin/*`)

| Pantalla | Ruta | Requerimientos |
|---|---|---|
| Dashboard de indicadores con gráficos (Chart.js, copia local) | `/admin/dashboard` | RF-42, RF-53 |
| Usuarios del personal: búsqueda, filtro por rol y paginación; crear, editar rol y estado, restablecer contraseña, desbloquear | `/admin/usuarios` | RF-37, RF-39, RF-40, RF-55 |
| Clientes: búsqueda y paginación, activación y **detalle** con todas sus compras y boletos | `/admin/clientes`, `?accion=ver&id=` | RF-37, RF-54, RF-55 |
| Reportes: general por evento, ventas (por zona, canal y vendedor), asistencia, ocupación e incidencias; filtros por fechas, evento, tipo, categoría y zona; PDF en pestaña nueva y descarga CSV | `/reportes` | RF-43 a RF-49 |

### Cliente (`/cliente/*`; la cartelera y el detalle del evento son públicos)

| Pantalla | Ruta | Requerimientos |
|---|---|---|
| Crear cuenta (sin documento ni correo repetidos) | `/registro` | RF-13, RF-14 |
| Cartelera con búsqueda y filtros por tipo, categoría, recinto y fechas (pública en el inicio y también dentro del panel del cliente) | `/`, `/cliente/cartelera` | RF-15 |
| Detalle público del evento: precios y disponibilidad por zona | `/evento?id=` | RF-15 |
| Elegir asientos en tres columnas: datos del evento con zonas, precios y disponibilidad a la izquierda; mapa fijo en el centro con solo los asientos de la zona elegida; zonas arriba y la selección o reserva abajo a la derecha. Reserva temporal con cuenta regresiva | `/cliente/comprar?evento=` | RF-16 a RF-19 |
| Pago con tarjeta (única forma de pago en línea), al estilo de una pasarela: datos de facturación, tarjeta con vista previa, resumen del pedido, "Procesando pago…" y confirmación. La validación es ficticia: solo exige que los datos estén escritos y los datos de la tarjeta no se guardan | `/cliente/pago?evento=` | RF-17, RF-20, RF-21 |
| Mis boletos: boletos con QR, comprobante PDF y anulación con motivo | `/cliente/boletos` | RF-20, RF-22, RF-23 |
| Mi perfil y cambio de contraseña | `/cliente/perfil` | RF-13 |

El Administrador también tiene **Venta en mostrador** (`/admin/mostrador`, RF-24): busca al cliente por documento, elige los asientos en el mismo mapa y registra la venta con canal Mostrador.

### Control de acceso (`/puerta/*`; el Administrador también tiene acceso)

| Pantalla | Ruta | Requerimientos |
|---|---|---|
| Validar boletos por código o QR (lector, teclado o cámara) y por documento; bitácora y anulación de ingresos | `/puerta/validar` | RF-25 a RF-31 |
| Ocupación en vivo por zona (se actualiza sola con AJAX cada 10 s) | `/puerta/ocupacion` | RF-28 |
| Incidencias: registro, seguimiento y consulta con filtros | `/puerta/incidencias` | RF-32 a RF-35 |

### Común

Las opciones de cada rol están en un **menú lateral**; la barra superior solo tiene la marca, los enlaces públicos y la sesión. En pantallas angostas el menú pasa a una franja horizontal.
Las confirmaciones (publicar, cancelar, desactivar, eliminar) usan **SweetAlert2** (copia local en `resources/js`, sin depender de internet) en lugar de los cuadros del navegador.

Inicio y cierre de sesión con cookie "recordar usuario" (RF-36, RF-41), bloqueo de 15 minutos tras 5 intentos fallidos (RF-40), expiración de la sesión a los 20 minutos de inactividad, páginas de error 403/404/500 y página de inicio pública con la cartelera de eventos en tarjetas (el afiche se sirve en `/afiche?evento=ID`; sin afiche se usa una imagen por defecto).

## Seguridad

- Contraseñas con **BCrypt** (hash con salt). Nunca se guardan ni se muestran en texto plano.
- **Filtro de autorización:** `/admin/*` y `/reportes` solo admiten Administrador; `/organizador/*` admite Organizador y Administrador; `/cliente/*` solo Cliente; `/puerta/*` Control de acceso y Administrador. Cualquier otro rol recibe 403.
- **Control de propiedad (RN-13):** los boletos, QR y comprobantes se consultan siempre filtrando por el cliente en sesión, así que cambiar un id en la URL no da acceso a boletos ajenos (responde 404).
- **Códigos de boleto** cortos (`TK-XXXX`) generados con `SecureRandom`, con un alfabeto sin caracteres que se confunden (sin 0/O ni 1/I). Si sale un código repetido, el `UNIQUE` de `entrada.codigo` lo rechaza y se genera otro.
- **Token CSRF** en todos los formularios POST, y cierre de sesión solo por POST.
- Consultas parametrizadas (sin concatenar datos del usuario) y salida escapada con `c:out` (XSS).
- Afiches adjuntos validados por su contenido real (firma de JPG, PNG, WEBP o GIF) y con un máximo de 2 MB, no por la extensión del archivo.
- Se descarta la sesión anónima al iniciar sesión (contra fijación de sesión), y la cookie de sesión es `HttpOnly`.
- Credenciales de MySQL en un archivo local tipo `.env` (`src/main/resources/db.properties`, en `.gitignore`).

## Reglas de negocio destacadas

- **Un asiento = un boleto (RN-01):** `evento_asiento` tiene `UNIQUE (id_evento, id_asiento)`.
- **Publicar (RF-07):** en una sola transacción se verifica que todas las zonas tengan precio y que no haya choque de horario, se copian los asientos activos del recinto al evento y el evento pasa a *Publicado*.
- **Choque de horario (RN-05):** un recinto no puede tener dos eventos activos traslapados.
- **Cancelar (RN-09):** en una transacción se anulan las ventas y los boletos, se liberan los asientos y el evento pasa a *Cancelado*.
- **Agotado (RN-11):** se recalcula al bloquear o desbloquear asientos, al comprar y al anular.
- **Compra sin colisiones (RNF-05):** reservar y vender usan una actualización condicional (`UPDATE ... WHERE estado = 'Disponible'`); si dos clientes eligen el mismo asiento a la vez, solo uno lo obtiene y el otro ve cuál perdió.
- **Un solo ingreso (RN-07):** al validar, la fila del boleto se bloquea (`SELECT ... FOR UPDATE`); si dos puertas leen el mismo QR al mismo tiempo, una lo acepta y la otra lo rechaza como "ya utilizado".

## Pruebas automatizadas

```bash
mvn test
```

Son 58 pruebas JUnit, sin base de datos:

| Clase de prueba | Qué cubre |
|---|---|
| `ValidadorUsuarioTest` | Formato de usuario y política de contraseñas |
| `SeguridadClaveTest` | Hash BCrypt y verificación |
| `AutenticacionServicioTest` | Inicio de sesión, cuenta desactivada y bloqueo al quinto intento (DAO en memoria) |
| `AutenticacionFilterTest` | Matriz de permisos por rol (`/reportes` solo para el Administrador) |
| `ValidadorEventoTest` | Fechas del evento y regla de traslape de horarios |
| `GeneradorAsientosTest` | Cálculo de filas y límites; flujo de estados del evento |
| `ExportadorCsvTest` | Formato CSV, comillas y protección contra fórmulas |
| `ExportadorPdfTest` | PDF formal con datos y resumen, agrupado por secciones y vacío |
| `JsonTest` | Series de los gráficos y escape de comillas y etiquetas HTML |
| `ReglasCompraTest` | Venta abierta, máximo por compra y plazo de anulación (RN-03, RN-04, RN-12) |
| `ValidadorClienteTest` | Datos del registro del cliente |
| `GeneradorCodigoTest` | Formato y unicidad de los códigos de boleto e imagen QR |
| `ValidadorPagoTest` | Pago simulado: solo exige datos escritos, sin revisar dígitos ni la tarjeta |
| `ValidadorAccesoTest` | Rechazos en puerta, horario, re-entrada y transiciones de incidencias |
| `ValidadorImagenTest` | Afiche: tipo real por firma de bytes, archivo falso y límite de 2 MB |

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
| 8b | Guardar un evento sin el precio de una zona con asientos | "Escriba el precio de la zona…"; lo escrito se conserva | Verificado |
| 9 | Evento en el mismo recinto y horario | Rechazado por choque de horario (RN-05) | Verificado |
| 10 | Bloquear un asiento vendido | Rechazado; solo se bloquean disponibles | Verificado |
| 11 | Cambiar la distribución de una zona (reducir y ampliar filas o asientos por fila) | Crea los que faltan e inactiva, sin borrar, los que quedan fuera; informa cuántos | Verificado |
| 11b | Guardar una zona cambiando solo el nombre | No toca los asientos (se conservan los inhabilitados uno por uno) | Verificado |
| 12 | Cancelar evento con ventas | 22 boletos anulados y asientos liberados | Verificado |
| 13 | Reporte de asistencia en CSV | Archivo con BOM UTF-8 y totales | Verificado |
| 14 | Tildes y ñ en formularios | Se guardan y muestran correctamente | Verificado |
| 15 | Adjuntar afiche PNG a un evento | Se guarda en MySQL y se ve en la cartelera y el detalle | Verificado |
| 16 | Adjuntar un archivo de texto renombrado como `.png` | "El afiche debe ser una imagen JPG, PNG, WEBP o GIF" | Verificado |
| 17 | Afiche de más de 2 MB | Mensaje de tamaño máximo; el evento no se modifica | Verificado |
| 18 | Editar sin adjuntar / marcar "Quitar el afiche actual" | Conserva el afiche / vuelve a la imagen por defecto | Verificado |
| 19 | Duplicar un evento y guardarlo con otras fechas | Nuevo evento en Borrador con los mismos precios por zona | Verificado |
| 20 | Desactivar un evento publicado / uno en Borrador | Rechazado (debe cancelarse antes) / desaparece del listado | Verificado |
| 21 | Reporte de ventas filtrado por tipo y exportado a PDF | PDF con la tabla, los totales y los filtros aplicados | Verificado |
| 22 | Reporte de ocupación o asistencia filtrado por zona | Solo cuenta los asientos de esa zona | Verificado |
| 23 | Registrarse con un documento ya usado | "Ya existe una cuenta con ese documento" | Verificado |
| 24 | Dos clientes reservan el mismo asiento al mismo tiempo | Solo uno lo obtiene; el otro ve "Estos asientos ya no están disponibles: Sol General A-1" | Verificado |
| 25 | Comprar la reserva con forma de pago | Venta Web, un boleto con código y QR por asiento | Verificado |
| 26 | Pedir el QR o el comprobante de otro cliente cambiando el id | 404 | Verificado |
| 27 | Anular una compra dentro del plazo | Venta y boletos anulados, asientos liberados | Verificado |
| 28 | Venta en mostrador | Venta Mostrador con el Administrador como vendedor | Verificado |
| 29 | Validar en puerta: boleto válido, repetido, falso y de otro evento | VÁLIDO con asiento / "Boleto ya utilizado" / "Código inexistente" / "Boleto de otro evento" | Verificado |
| 30 | Dos puertas leen el mismo QR a la vez | Uno válido y uno rechazado | Verificado |
| 31 | Anular un ingreso por error | El boleto vuelve a Emitida | Verificado |
| 32 | Cerrar una incidencia sin observaciones | "Escriba cómo se resolvió la incidencia antes de cerrarla" | Verificado |

## Control de credenciales

- `src/main/resources/db.properties` (contraseña de MySQL) es local, como un `.env`, y está en `.gitignore`. En el repositorio solo va `db.properties.example`.
- `sql/00_usuario_aplicacion.sql` solo trae un marcador (`CAMBIAR_LOCALMENTE`).
- El repositorio no contiene usuarios con contraseña: el administrador se crea en la configuración inicial.
