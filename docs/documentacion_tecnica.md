# TICKA — Documentación técnica

**Sistema de Eventos en Vivo** · ITCA-FEPADE · Desarrollo de Aplicaciones Multiplataforma (COIDS3504) · Grupo IDSN421 · Equipo 11

Repositorio: <https://github.com/Jozeeh/TICKA>

## 1. Resumen

TICKA es una aplicación web para administrar espectáculos en vivo (conciertos, partidos, ópera y teatro) y vender boletos **por asiento**. Cubre todo el ciclo: se crea el recinto con sus zonas y asientos, se publica el evento con precios por zona, el cliente elige sus asientos en un mapa y paga, recibe un boleto con código QR, y el personal lo valida en la puerta. La administración consulta indicadores y reportes.

> **En una frase:** es una aplicación web Java con **Servlets y JSP**, arquitectura **MVC por capas** con patrón **DAO**, desplegada en **Tomcat 9** y con base de datos **MySQL 8**. No usa frameworks como Spring ni Hibernate.

## 2. Ficha técnica

| Aspecto | Tecnología |
|---|---|
| Lenguaje | Java 11 |
| Tipo de aplicación | Web, empaquetada como WAR (`ticka.war`) |
| Construcción | Maven |
| Servidor | Apache Tomcat 9 |
| API web | Servlets y JSP con paquetes `javax.servlet.*` (Java EE 8, mediante `jakarta.jakartaee-api` 8.0.0) |
| Vistas | JSP + JSTL 1.2 con Bootstrap 5.3.3 (copia local) y JavaScript sin frameworks; `estilos.css` solo agrega los colores de la marca y las piezas propias (mapa de asientos, calendario, boletos) |
| Acceso a datos | JDBC con `PreparedStatement` y patrón DAO (SQL escrito a mano, sin ORM) |
| Base de datos | MySQL 8 (`mysql-connector-j` 8.4.0) |
| Contraseñas | BCrypt, costo 10 (jBCrypt 0.4) |
| Códigos QR | ZXing 3.5.3 |
| PDF | OpenPDF 1.3.43 (comprobantes y reportes) |
| Gráficos y alertas | Chart.js y SweetAlert2 (copias locales) |
| Pruebas | JUnit 4.13.2 (58 pruebas de reglas de negocio) |

### ¿Es Jakarta o Servlets/JSP?

Es **Servlets/JSP clásico**. La dependencia se llama `jakarta.jakartaee-api`, pero su versión 8.0.0 todavía expone los paquetes `javax.*`; por eso el código usa `javax.servlet.*` y funciona en Tomcat 9. Tomcat 10 o superior usa `jakarta.servlet.*` y **no es compatible** con este proyecto.

## 3. Arquitectura

Patrón **MVC en capas**. Cada capa solo conoce a la siguiente:

```text
Navegador
   │  petición HTTP
   ▼
Filtros          Codificación UTF-8 → protección CSRF → autenticación y rol
   ▼
Controlador      Servlets: leen parámetros y eligen la vista o redirección
   ▼
Servicio         Reglas de negocio, validaciones y transacciones
   ▼
DAO              Consultas SQL con JDBC (interfaz + implementación)
   ▼
MySQL

El Servlet pasa los datos (Modelo) a la vista JSP, que genera el HTML de respuesta.
```

| Capa | Paquete (`sv.edu.itca.ticka`) | Responsabilidad | Archivos |
|---|---|---|---|
| Controlador | `controlador` (`admin`, `organizador`, `cliente`, `puerta`) | Servlets con `@WebServlet`; atienden GET y POST | 29 |
| Servicio | `servicio` | Reglas de negocio, validadores y transacciones | 20 |
| DAO | `dao` y `dao.impl` | Interfaces e implementaciones JDBC | 23 |
| Modelo | `modelo` | Objetos de datos y enumeraciones (`Rol`, `EstadoEvento`) | 29 |
| Filtros | `filtro` | Codificación, CSRF y control de acceso | 3 |
| Persistencia | `persistencia` | `ConexionBD`: abre las conexiones a MySQL | 1 |
| Configuración | `config` | `AplicacionListener`: tarea programada y cierre limpio | 1 |
| Utilidades | `util` | PDF, CSV, QR, códigos, formatos y sesión | 10 |
| Vistas | `WEB-INF/vistas` | Páginas JSP y plantillas `.jspf` comunes | 45 |

Las JSP están dentro de `WEB-INF`, así que el navegador no puede abrirlas directamente: siempre se llega a ellas por un Servlet.

### Ejemplo de flujo: comprar un boleto

1. El cliente marca asientos en `/cliente/comprar` y envía el formulario.
2. `CompraServlet` recibe la petición y llama a `CompraServicio.reservar()`.
3. El servicio abre una transacción y aparta los asientos por 10 minutos mediante `VentaDAO`.
4. El cliente llega a `/cliente/pago`, llena facturación y tarjeta, y `PagoServlet` llama a `CompraServicio.comprar()`.
5. Se registran la venta y los boletos, cada uno con un código `TK-XXXX`.
6. El cliente ve su boleto con QR en `/cliente/boletos`, y en la puerta `/puerta/validar` lo marca como usado.

## 4. Roles y módulos

| Rol | Ruta | Funciones |
|---|---|---|
| **Administrador** | `/admin/*`, `/reportes` y además lo del Organizador y Control de acceso | Usuarios, clientes, dashboard, reportes, venta en mostrador |
| **Organizador** | `/organizador/*` | Eventos, calendario, recintos, zonas, asientos, categorías |
| **Cliente** | `/cliente/*` | Registro, cartelera, mapa de asientos, carrito, pago, boletos con QR, anulación, perfil |
| **Control de acceso** | `/puerta/*` | Validar boletos, ocupación en vivo, incidencias |

El personal cambia su nombre y contraseña en `/perfil`; el cliente, en `/cliente/perfil`.

## 5. Base de datos

Base `ticka` con 14 tablas y una vista:

| Grupo | Tablas |
|---|---|
| Cuentas | `rol`, `usuario`, `cliente` |
| Recintos | `recinto`, `zona`, `asiento` |
| Eventos | `categoria`, `evento`, `precio_zona`, `evento_asiento` |
| Ventas | `venta`, `entrada` |
| Puerta | `acceso`, `incidencia` |
| Vista | `v_ocupacion_evento_zona` |

`evento_asiento` guarda el estado de cada asiento en cada evento. El diagrama completo está en [`diagrama_er.md`](diagrama_er.md).

Scripts en `sql/`: `00` usuario de la aplicación, `01` esquema, `02` datos de prueba, `03` y `04` migraciones para bases antiguas.

## 6. Decisiones técnicas importantes

### Dos personas no pueden comprar el mismo asiento

- La reserva y la venta usan un `UPDATE` condicional: solo tiene efecto si el asiento sigue libre.
- La reserva completa ocurre en una transacción; si un asiento falla, no se aparta ninguno.
- En la puerta, `SELECT ... FOR UPDATE` bloquea la fila del boleto: si dos puertas leen el mismo código, la segunda lo ve como usado.

### Reservas temporales

Los asientos quedan apartados 10 minutos. Un hilo programado en `AplicacionListener` se ejecuta cada minuto, libera las reservas vencidas y actualiza el estado de los eventos (a En curso o Finalizado).

### Seguridad

| Riesgo | Medida |
|---|---|
| Contraseñas expuestas | Hash BCrypt; nunca se guardan en texto plano |
| Fuerza bruta | Bloqueo de 15 minutos tras 5 intentos fallidos |
| Inyección SQL | `PreparedStatement` en todas las consultas |
| XSS | Salida escapada con `<c:out>` en datos de usuarios |
| CSRF | Token por sesión, obligatorio en todo POST |
| Acceso indebido | `AutenticacionFilter` valida sesión y rol por ruta |
| Robo o fijación de sesión | Cookie `HttpOnly`, sesión nueva al iniciar sesión, expira a los 20 minutos |
| Credenciales en Git | `db.properties` está fuera del repositorio; la app usa un usuario MySQL sin privilegios de root |

### Parámetros configurables (`ticka.properties`)

| Parámetro | Valor | Significado |
|---|---|---|
| `reserva.minutos` | 10 | Duración de la reserva |
| `compra.maximo` | 6 | Asientos máximos por compra |
| `anulacion.horas` | 24 | Límite para anular antes de abrir puertas |
| `acceso.reentrada` | false | Si un boleto usado puede volver a ingresar |
| `acceso.incidenciaPorRechazo` | true | Registrar incidencia por cada rechazo en puerta |

## 7. Alcance y limitaciones

- **El pago es simulado:** no hay pasarela real. Solo se revisa que los campos estén llenos y los datos de tarjeta no se guardan.
- **Conexiones sin pool:** cada operación abre su conexión con `DriverManager`. Es suficiente para el proyecto; en producción convendría un pool.
- **Es una aplicación web responsiva**, no una aplicación móvil nativa.

## 8. Ejecución

Requisitos: JDK 11, Maven, Tomcat 9 y MySQL 8.

```bash
mysql -u root -p < sql/01_esquema_ticka.sql
```

```bash
mysql -u root -p < sql/02_datos_prueba.sql
```

```bash
mvn clean package
```

Antes de compilar, ejecute `sql/00_usuario_aplicacion.sql` con una contraseña propia y copie `db.properties.example` como `db.properties`. Luego despliegue `target/ticka.war` en Tomcat y abra <http://localhost:8080/ticka/>. El detalle está en el [`README`](../README.md).

Cuentas de prueba, todas con contraseña `itca1234`:

| Usuario | Rol |
|---|---|
| `admin` | Administrador |
| `organizador` | Organizador |
| `puerta` | Control de acceso |
| `cliente` | Cliente |

## 9. Respuestas rápidas para la defensa

| Pregunta | Respuesta |
|---|---|
| ¿Qué arquitectura usa? | MVC en capas: Servlets, servicios, DAO y JSP |
| ¿Usa Spring o Hibernate? | No. Servlets, JSP y JDBC directo |
| ¿Con qué se hizo el diseño? | Con Bootstrap 5 (rejilla, tarjetas, formularios, tablas, menú). Chart.js para gráficos y SweetAlert2 para las alertas |
| ¿`javax` o `jakarta`? | `javax`, por eso requiere Tomcat 9 |
| ¿Cómo se accede a la base? | JDBC con `PreparedStatement`, a través de interfaces DAO |
| ¿Cómo evitan la doble venta? | `UPDATE` condicional dentro de transacciones |
| ¿Cómo se guardan las contraseñas? | Con hash BCrypt |
| ¿Cómo se controlan los permisos? | Con un filtro que revisa la sesión y el rol según la ruta |
| ¿El pago es real? | No, es una simulación con validación ficticia |
