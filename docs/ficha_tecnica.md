# Ficha técnica — TICKA

**Sistema de Eventos en Vivo** · ITCA-FEPADE · Desarrollo de Aplicaciones Multiplataforma (COIDS3504) · Grupo IDSN421 · Equipo 11
Repositorio: <https://github.com/Jozeeh/TICKA>

## 1. Datos generales

| Campo | Valor |
|---|---|
| Nombre | TICKA — Sistema de Eventos en Vivo |
| Tipo | Aplicación web responsiva (no es app móvil nativa) |
| Propósito | Venta de boletos **por asiento** para conciertos, partidos, ópera y teatro, con QR y control de acceso en puerta |
| Arquitectura | MVC en capas: Filtros → Servlets → Servicios → DAO → MySQL; vistas JSP |
| Paquete base | `sv.edu.itca.ticka` |
| Artefacto Maven | `sv.edu.itca:TICKA:1.0-SNAPSHOT` → `ticka.war` |
| URL local | <http://localhost:8080/ticka/> (contexto `/ticka`) |
| URL en producción | <https://ticka.site> (también <https://ticka.jozeeh.dpdns.org>) |
| Roles | Administrador, Organizador, Cliente, Control de acceso |

## 2. Tecnologías y versiones

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java (JDK) | 11 (`maven.compiler.release=11`) |
| Construcción | Maven (WAR) | compiler-plugin 3.13.0 · war-plugin 3.4.0 |
| Servidor | Apache Tomcat | 9.0.x (usa `javax.*`; Tomcat 10+ **no** es compatible) |
| API web | Servlets + JSP (`jakarta.jakartaee-api`, paquetes `javax.*`) | 8.0.0 (Java EE 8) |
| Etiquetas JSP | JSTL | 1.2 |
| Base de datos | MySQL | 8.x |
| Driver JDBC | `mysql-connector-j` | 8.4.0 |
| Contraseñas | jBCrypt (BCrypt, costo 10) | 0.4 |
| Códigos QR | ZXing `core` + `javase` | 3.5.3 |
| PDF | OpenPDF | 1.3.43 |
| Diseño | Bootstrap (CSS + JS bundle, copia local) | 5.3.3 |
| Gráficos | Chart.js (copia local) | 4 |
| Alertas y confirmaciones | SweetAlert2 (copia local) | 11.14.5 |
| Pruebas | JUnit | 4.13.2 |

**No usa** Spring, Hibernate/JPA ni frameworks de JavaScript. El SQL está escrito a mano con `PreparedStatement`.

## 3. Arquitectura

```text
Navegador ──HTTP──▶ Filtros ──▶ Servlet (controlador) ──▶ Servicio ──▶ DAO ──▶ MySQL
                    │                 │
                    │                 └──forward──▶ JSP (vista) ──HTML──▶ Navegador
                    └─ UTF-8 → CSRF → sesión y rol
```

| Capa | Responsabilidad | No hace |
|---|---|---|
| Filtro | Codificación UTF-8, token CSRF en todo POST, sesión y permiso por ruta | Lógica de negocio |
| Controlador (Servlet) | Lee parámetros, llama al servicio, elige vista o redirección (patrón PRG con mensajes flash) | SQL |
| Servicio | Reglas de negocio, validaciones, transacciones | HTML |
| DAO | Consultas SQL (interfaz + implementación JDBC) | Reglas de negocio |
| Modelo | Objetos de datos y enumeraciones | — |
| Vista (JSP) | Muestra datos con JSTL; escapa con `<c:out>` | Acceso a datos |

Las JSP están dentro de `WEB-INF`, así que solo se llega a ellas a través de un Servlet.

## 4. Estructura del proyecto

```text
TICKA/
├── pom.xml
├── README.md
├── docs/                      requerimientos, diagrama ER, guion de demo, documentación y esta ficha
├── sql/
│   ├── 00_usuario_aplicacion.sql   usuario MySQL de la app (sin privilegios de root)
│   ├── 01_esquema_ticka.sql        14 tablas + 1 vista
│   ├── 02_datos_prueba.sql         recintos, eventos, ventas y cuentas de prueba
│   └── 03_…, 04_…                  migraciones para bases antiguas
└── src/
    ├── main/
    │   ├── java/sv/edu/itca/ticka/
    │   │   ├── config/          AplicacionListener (tarea programada y cierre limpio)
    │   │   ├── controlador/     Servlets comunes (acceso, registro, panel, perfil, reportes…)
    │   │   │   ├── admin/       usuarios, clientes, dashboard, venta en mostrador
    │   │   │   ├── organizador/ eventos, calendario, recintos, zonas, mapa, categorías
    │   │   │   ├── cliente/     cartelera, comprar, carrito, pago, boletos, perfil
    │   │   │   └── puerta/      validar, ocupación, incidencias
    │   │   ├── filtro/          CodificacionFilter, CsrfFilter, AutenticacionFilter
    │   │   ├── servicio/        reglas de negocio y validadores
    │   │   ├── dao/             interfaces DAO + DAOException
    │   │   │   └── impl/        implementaciones JDBC
    │   │   ├── modelo/          entidades, enums (Rol, EstadoEvento, TipoEvento) y objetos de vista
    │   │   ├── persistencia/    ConexionBD
    │   │   └── util/            PDF, CSV, QR, JSON, códigos, formatos, sesión
    │   ├── resources/
    │   │   ├── ticka.properties        parámetros de negocio
    │   │   ├── db.properties.example   plantilla de conexión
    │   │   └── db.properties           conexión real (local, fuera de Git)
    │   └── webapp/
    │       ├── META-INF/context.xml    contexto /ticka
    │       ├── WEB-INF/web.xml         filtros, sesión, páginas de error
    │       ├── WEB-INF/vistas/         JSP por módulo + plantilla/ (fragmentos .jspf)
    │       └── resources/
    │           ├── css/   bootstrap.min.css, estilos.css
    │           ├── js/    bootstrap.bundle.min.js, chart.umd.min.js, sweetalert2.all.min.js, confirmaciones.js
    │           └── img/   portada.webp, sin-imagen.png
    └── test/java/sv/edu/itca/ticka/   pruebas JUnit (filtro, servicio, util)
```

### Cantidad de archivos

| Elemento | Cantidad |
|---|---|
| Servlets (controladores) | 29 |
| Filtros | 3 |
| Servicios y validadores | 20 |
| Interfaces DAO | 11 (+ `DAOException`) |
| Implementaciones DAO | 11 |
| Clases de modelo | 29 |
| Utilidades | 10 |
| Vistas JSP / fragmentos JSPF | 35 / 10 |
| Clases de prueba / pruebas | 14 / 58 |

## 5. Clases por paquete

| Paquete | Clases |
|---|---|
| `config` | AplicacionListener |
| `controlador` | AccesoServlet, AficheServlet, CerrarSesionServlet, ConfiguracionInicialServlet, EventoPublicoServlet, InicioServlet, PanelServlet, PerfilServlet, RegistroServlet, ReporteServlet |
| `controlador.admin` | ClienteServlet, DashboardServlet, MostradorServlet, UsuarioServlet |
| `controlador.organizador` | CalendarioServlet, CategoriaServlet, EventoServlet, MapaAsientosServlet, RecintoServlet, ZonaServlet |
| `controlador.cliente` | BoletosServlet, CarritoServlet, CarteleraServlet, CompraServlet, PagoServlet, PerfilServlet |
| `controlador.puerta` | IncidenciaServlet, OcupacionServlet, ValidarServlet |
| `filtro` | AutenticacionFilter, CodificacionFilter, CsrfFilter |
| `servicio` | AccesoServicio, AutenticacionServicio, ClienteServicio, CompraServicio, EventoServicio, IncidenciaServicio, UsuarioServicio, GeneradorAsientos, GeneradorCalendario, MapaAsientos, ReglasCompra, SeguridadClave, TareaEstadosEventos, ServicioException, ValidadorAcceso, ValidadorCliente, ValidadorEvento, ValidadorImagen, ValidadorPago, ValidadorUsuario |
| `dao` | AccesoDAO, AsientoEventoDAO, CategoriaDAO, ClienteDAO, EventoDAO, IncidenciaDAO, RecintoDAO, ReporteDAO, UsuarioDAO, VentaDAO, ZonaDAO, DAOException |
| `dao.impl` | Una clase `…DAOImpl` por cada interfaz |
| `modelo` | Usuario, Cliente, Rol, Recinto, Zona, Asiento, Categoria, Evento, EstadoEvento, TipoEvento, PrecioZona, Afiche, Incidencia, RegistroAcceso, ResultadoValidacion, BoletoPuerta, CompraCliente, VentaAnulable, AsientoSeleccionado, FilaAsientos, ZonaMapa, OcupacionZona, OcupacionPuerta, Indicadores, Reporte, FiltroEvento, FiltroReporte, Paginacion, DiaCalendario |
| `persistencia` | ConexionBD |
| `util` | ComprobantePdf, ExportadorPdf, ExportadorCsv, GeneradorQr, GeneradorCodigo, Json, Formatos, Configuracion, Parametros, Sesion |

## 6. Rutas y permisos

| Ruta | Servlet | Acceso |
|---|---|---|
| `/` · `/inicio` | InicioServlet | Público |
| `/evento` | EventoPublicoServlet | Público |
| `/afiche` | AficheServlet | Público (imagen del evento) |
| `/acceso` · `/registro` | AccesoServlet · RegistroServlet | Público |
| `/configuracion-inicial` | ConfiguracionInicialServlet | Solo si no existe administrador |
| `/cerrar-sesion` | CerrarSesionServlet | Con sesión |
| `/panel` · `/perfil` | PanelServlet · PerfilServlet | Con sesión (perfil: personal) |
| `/admin/usuarios`, `/admin/clientes`, `/admin/dashboard`, `/admin/mostrador` | admin/* | Administrador |
| `/reportes` | ReporteServlet | Administrador |
| `/organizador/eventos`, `/calendario`, `/recintos`, `/zonas`, `/mapa`, `/categorias` | organizador/* | Organizador y Administrador |
| `/cliente/cartelera`, `/comprar`, `/carrito`, `/pago`, `/boletos`, `/perfil` | cliente/* | Cliente |
| `/puerta/validar`, `/puerta/ocupacion`, `/puerta/incidencias` | puerta/* | Control de acceso y Administrador |

El permiso lo decide `AutenticacionFilter.tienePermiso(usuario, ruta)`. Sin sesión redirige a `/acceso`; con rol incorrecto responde 403.

## 7. Base de datos

Base `ticka` (MySQL 8, InnoDB, `utf8mb4`): 14 tablas y 1 vista.

| Grupo | Tablas | Descripción |
|---|---|---|
| Cuentas | `rol`, `usuario`, `cliente` | Usuarios del personal y clientes (hash BCrypt, bloqueo por intentos) |
| Recintos | `recinto`, `zona`, `asiento` | Lugares, zonas y asientos numerados por fila (plantilla) |
| Eventos | `categoria`, `evento`, `precio_zona`, `evento_asiento` | Evento, precio por zona y estado de cada asiento en cada evento |
| Ventas | `venta`, `entrada` | Compra (web o mostrador) y boleto con código `TK-XXXX` |
| Puerta | `acceso`, `incidencia` | Bitácora de lecturas en puerta e incidencias |
| Vista | `v_ocupacion_evento_zona` | Vendidos, reservados y disponibles por evento y zona |

Estados de un asiento en un evento: Disponible, Reservado, Vendido, Bloqueado.
Estados de un evento: Borrador → Publicado → Agotado / En curso → Finalizado, o Cancelado.

## 8. Configuración

| Archivo | Contenido |
|---|---|
| `db.properties` | `db.url`, `db.user`, `db.password` (local, en `.gitignore`; se copia desde `db.properties.example`) |
| Variables de entorno | `TICKA_DB_URL`, `TICKA_DB_USER`, `TICKA_DB_PASSWORD` (tienen prioridad sobre el archivo) |
| `ticka.properties` | Parámetros de negocio (tabla siguiente) |
| `web.xml` | Orden de filtros, sesión de 20 min con cookie `HttpOnly`, páginas de error 403/404/500 |
| `context.xml` | Ruta de contexto `/ticka` |

| Parámetro | Valor | Uso |
|---|---|---|
| `reserva.minutos` | 10 | Tiempo que un asiento queda apartado mientras se paga |
| `compra.maximo` | 6 | Asientos máximos por compra |
| `anulacion.horas` | 24 | Límite para anular antes de abrir puertas |
| `acceso.reentrada` | false | Si un boleto usado puede volver a entrar |
| `acceso.incidenciaPorRechazo` | true | Registra una incidencia por cada rechazo en puerta |

## 9. Seguridad

| Riesgo | Medida |
|---|---|
| Contraseñas expuestas | Hash BCrypt (costo 10); nunca en texto plano |
| Fuerza bruta | Bloqueo de 15 minutos tras 5 intentos fallidos |
| Inyección SQL | `PreparedStatement` en todas las consultas |
| XSS | Salida escapada con `<c:out>` |
| CSRF | Token por sesión obligatorio en todo POST (`CsrfFilter`) |
| Acceso indebido | `AutenticacionFilter` por ruta y rol; JSP dentro de `WEB-INF` |
| Robo o fijación de sesión | Cookie `HttpOnly`, sesión nueva al iniciar sesión, expira a los 20 min |
| Archivos maliciosos | El afiche se valida por tamaño (2 MB) y tipo real de imagen (`ValidadorImagen`); la petición multipart está limitada con `@MultipartConfig` |
| Credenciales en Git | `db.properties` fuera del repositorio; usuario MySQL propio sin privilegios de root |

## 10. Decisiones técnicas clave

- **Sin doble venta:** reservar y vender usan un `UPDATE` condicional (solo cambia si el asiento sigue libre) dentro de una transacción; si un asiento falla, no se aparta ninguno.
- **Puerta concurrente:** `SELECT … FOR UPDATE` bloquea el boleto; si dos puertas lo leen a la vez, la segunda lo ve como usado.
- **Reservas que vencen:** `AplicacionListener` ejecuta cada minuto `TareaEstadosEventos`, que libera reservas vencidas y pasa eventos a En curso o Finalizado.
- **Carrito persistente:** la reserva vive en la base de datos, así que el carrito sigue vigente aunque se cierre sesión.
- **Pago simulado:** no hay pasarela; solo se validan los campos y no se guardan datos de tarjeta.
- **Conexiones:** `DriverManager` por operación (sin pool), suficiente para el alcance del proyecto.

## 11. Interfaz (frontend)

| Elemento | Detalle |
|---|---|
| Plantillas comunes | `head.jspf` (Bootstrap y estilos), `navbar.jspf` (barra superior y menú lateral por rol), `pie.jspf`, `alertas.jspf`, `cartelera.jspf`, `mapa-seleccion.jspf` |
| Maquetación | Bootstrap 5.3: rejilla, tarjetas, formularios, tablas, alertas, badges, paginación, pestañas, `navbar` y `offcanvas` (menú en celular) |
| `estilos.css` | Colores de la marca sobre Bootstrap y piezas propias: mapa de asientos, calendario, boletos QR, tarjeta de pago, pantallas de acceso y portada |
| JavaScript | Sin frameworks: cuenta regresiva de la reserva, mapa con zoom, ocupación en vivo con `fetch` cada 10 s, lectura de QR con la cámara (`BarcodeDetector`) |
| Librerías JS | Chart.js (dashboard), SweetAlert2 (confirmaciones con `data-confirmar`) |
| Colores | Azul `#1f3a5f` (principal), naranja `#e4572e` (acento), verde, amarillo y rojo para estados |

## 12. Pruebas

58 pruebas JUnit 4 en 14 clases, sin base de datos (reglas puras):

| Paquete | Clases de prueba |
|---|---|
| `filtro` | AutenticacionFilterTest (matriz de permisos por rol) |
| `servicio` | AutenticacionServicioTest, GeneradorAsientosTest, ReglasCompraTest, SeguridadClaveTest, ValidadorAccesoTest, ValidadorClienteTest, ValidadorEventoTest, ValidadorImagenTest, ValidadorPagoTest, ValidadorUsuarioTest |
| `util` | ExportadorCsvTest, ExportadorPdfTest, GeneradorCodigoTest, JsonTest |

## 13. Instalación y ejecución

Requisitos: JDK 11, Maven, Tomcat 9 y MySQL 8.

1. Crear la base: `sql/01_esquema_ticka.sql` y luego `sql/02_datos_prueba.sql`.
2. Crear el usuario de la aplicación con `sql/00_usuario_aplicacion.sql` (con contraseña propia).
3. Copiar `db.properties.example` como `db.properties` y completar la conexión.
4. Compilar con `mvn clean package` (ejecuta las pruebas).
5. Desplegar `target/ticka.war` en Tomcat 9 y abrir <http://localhost:8080/ticka/>.

Cuentas de prueba (contraseña `itca1234`): `admin`, `organizador`, `puerta`, `cliente`.

## 14. Despliegue en producción (VPS con Coolify)

El sitio está publicado en un **VPS propio** administrado con **[Coolify](https://coolify.io/)**, una plataforma
autohospedada que construye y ejecuta las aplicaciones en contenedores **Docker**. El panel de Coolify está en
`deploys.jozeeh.dpdns.org` (con inicio de sesión).

### Arquitectura del despliegue

```text
Usuario ──HTTPS──▶ Cloudflare (DNS) ──▶ VPS
                                         └─ Proxy de Coolify (enruta por dominio, HTTPS, compresión gzip)
                                              └─ Contenedor ticka-web  (Tomcat 9, puerto 8080)
                                                   └─ red interna Docker ──▶ Contenedor ticka-db (MySQL 8.4)
```

| Elemento | Valor |
|---|---|
| Servidor | VPS propio con Coolify (servidor `localhost` de Coolify) |
| Proyecto en Coolify | `TICKA` · entorno `production` · 2 recursos |
| Aplicación | `ticka-web` — *TICKA - Sistema de eventos en vivo (Tomcat 9 + MySQL)* |
| Base de datos | `ticka-db` — recurso MySQL administrado por Coolify |
| Dominios | `https://ticka.site` y `https://ticka.jozeeh.dpdns.org` |
| DNS | Cloudflare |
| HTTPS | Certificado de Let's Encrypt |

### Aplicación `ticka-web`

| Ajuste | Valor |
|---|---|
| Tipo de construcción | **Dockerfile** escrito en Coolify (el repositorio no necesita archivos de Docker) |
| Origen del código | Se clona `https://github.com/Jozeeh/TICKA.git`, rama `main` |
| Puerto expuesto | 8080 |
| Contexto | La app se publica como `ROOT.war`, por eso abre en la raíz del dominio (`/`) y no en `/ticka` |
| Zona horaria | `America/El_Salvador` (variable `TZ` y opción de la JVM) |
| Memoria de la JVM | `-Xms256m -Xmx768m` |
| Variables de entorno | `TICKA_DB_URL`, `TICKA_DB_USER`, `TICKA_DB_PASSWORD`, `TZ` |

La conexión a la base se toma de las variables `TICKA_DB_*`, que tienen prioridad sobre `db.properties`
(ver `ConexionBD`). Por eso no hace falta subir `db.properties` al servidor ni al repositorio, y la
contraseña solo existe en Coolify.

Dockerfile usado (construcción en dos etapas):

```dockerfile
# TICKA - Java 11 + Tomcat 9 (javax.servlet), WAR
# El codigo se clona desde GitHub para no requerir cambios en el repositorio.
FROM maven:3.9-eclipse-temurin-17 AS build
RUN apt-get update \
 && apt-get install -y --no-install-recommends git ca-certificates \
 && rm -rf /var/lib/apt/lists/*
ARG TICKA_REPO=https://github.com/Jozeeh/TICKA.git
ARG TICKA_REF=main
RUN git clone --depth 1 --branch "${TICKA_REF}" "${TICKA_REPO}" /src
WORKDIR /src
RUN mvn -q -B -DskipTests package

FROM tomcat:9.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/ticka.war /usr/local/tomcat/webapps/ROOT.war
ENV TZ=America/El_Salvador \
    CATALINA_OPTS="-Xms256m -Xmx768m -Dfile.encoding=UTF-8 -Duser.timezone=America/El_Salvador"
EXPOSE 8080
```

| Etapa | Imagen | Qué hace |
|---|---|---|
| 1. `build` | `maven:3.9-eclipse-temurin-17` | Clona el repositorio y compila el WAR con Maven (el código apunta a Java 11 con `release 11`) |
| 2. Ejecución | `tomcat:9.0-jdk17-temurin` | Tomcat 9 oficial (`javax.servlet`); borra las apps de ejemplo y copia `ticka.war` como `ROOT.war` |

La imagen final solo lleva Tomcat y el WAR: Maven y el código fuente se quedan en la primera etapa.

### Base de datos `ticka-db`

| Ajuste | Valor |
|---|---|
| Imagen | `mysql:8.4` |
| Base inicial | `ticka` |
| Usuario de la aplicación | `ticka_app` (no se usa `root`) |
| Acceso | **Privado**: sin puerto público, solo alcanzable desde la red interna de Docker de Coolify |
| Esquema y datos | Scripts de `sql/` (`01_esquema_ticka.sql` y `02_datos_prueba.sql`) |

### Cómo se publica una nueva versión

1. Se sube el cambio a la rama `main` de GitHub.
2. En Coolify, en `ticka-web`, se pulsa **Redeploy**.
3. Coolify vuelve a construir la imagen (clona `main`, compila el WAR) y reemplaza el contenedor.
4. La base `ticka-db` no se toca: los datos se conservan entre despliegues.

> En el Dockerfile la compilación usa `-DskipTests` para que el despliegue sea rápido; las 58 pruebas se
> ejecutan localmente con `mvn clean package` antes de subir los cambios.

## 15. Documentación relacionada

| Archivo | Contenido |
|---|---|
| `README.md` | Instalación detallada y lista de funciones |
| `docs/requerimientos.md` | Requerimientos funcionales (RF) y reglas de negocio (RN) |
| `docs/diagrama_er.md` | Diagrama entidad-relación |
| `docs/documentacion_tecnica.md` | Explicación de la arquitectura y respuestas rápidas para la defensa |
| `docs/guion_demo.md` | Guion paso a paso para la demostración |
| `docs/ficha_tecnica.md` | Esta ficha |
