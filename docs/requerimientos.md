# TICKA — Sistema de Eventos en Vivo
### Documento de Requerimientos

| Campo | Valor |
|---|---|
| Módulo | Desarrollo de Aplicaciones Multiplataforma (COIDS3504) |
| Grupo / Equipo | IDSN421 / Equipo 11 |
| Institución | ITCA-FEPADE |
| Nombre del proyecto (propuesto) | **TICKA** — *el equipo debe confirmarlo* |
| Ruta de contexto (propuesta) | `/ticka` |
| Descripción corta | Sistema web de gestión de eventos en vivo (conciertos, partidos, ópera y teatro) con venta de boletos por asiento |
| Sector | Entretenimiento en vivo — boletería (*ticketing*) |
| Versión del documento | 0.6 (borrador, semana 11/16) |
| Fecha | 2026-10-12 |

> **Nota:** nombre, módulos, entidades y prioridades son una propuesta de trabajo. No provienen de la guía; el equipo las decide y ajusta.

---

## 1. Introducción

### 1.1 Propósito
Definir los requerimientos funcionales, no funcionales y de datos de **TICKA**, sistema web que administra espectáculos en vivo (conciertos, partidos, ópera, teatro) y vende **boletos por asiento**: cartelera, recintos con mapa de asientos, compra con reserva temporal, control de acceso e indicadores. Es el proyecto integrador del ciclo y la base para diseño, implementación, pruebas y evaluación.

### 1.2 Problema empresarial
Las productoras, clubes deportivos y recintos pequeños y medianos administran su boletería con hojas de cálculo, mensajes y boletos impresos sin control central. Esto provoca:

- **Venta duplicada de asientos**: el mismo asiento se vende a dos personas porque cada canal lleva su propio registro.
- **Boletos falsificados o reutilizados**, que permiten ingresar más de una vez con la misma entrada.
- **Compras abandonadas que bloquean asientos** o, al contrario, asientos que se pierden por no existir una reserva temporal ordenada.
- **Filas lentas en puerta**, por validación manual y sin registro confiable de quién ingresó y a qué asiento.
- **Choques de horario**: dos eventos programados en el mismo recinto a la misma hora.
- **Incidentes sin trazabilidad** (seguridad, atención médica, asientos en disputa) y **ausencia de indicadores** de ventas, ocupación por zona y asistencia real (vendidos vs. ingresados).

Centralizar la operación en una aplicación web con base de datos permite garantizar que **un asiento = un boleto**, reservar y comprar sin colisiones, validar boletos con código/QR de un solo uso, detectar choques de horario en los recintos y producir reportes confiables.

### 1.3 Usuarios beneficiados
- **Productora/organizador:** planifica eventos y recintos sin choques de horario, y fija precios por zona.
- **Cliente (público asistente):** consulta la cartelera, elige su asiento en un mapa, compra y tiene su boleto con QR siempre disponible.
- **Personal de acceso y seguridad:** valida boletos rápido, ve el asiento correspondiente y registra incidentes.
- **Administración/gerencia:** controla usuarios y consulta ventas, ocupación y asistencia.

### 1.4 Alcance
**Incluye:** eventos de varios tipos (concierto, partido, ópera, teatro), recintos con zonas y asientos numerados, precios por zona, descripción y participantes de cada evento, portal del cliente (registro, cartelera, **selección de asientos en una cuadrícula**, reserva temporal, compra, "mis boletos" y anulación con código QR), venta en mostrador por el administrador, control de acceso, incidencias, usuarios y roles, dashboard y reportes con exportación.

**Fuera de alcance:** programación detallada por presentaciones y asignación de recursos (escenario, sonido, staff), bitácora de auditoría, pasarela de pagos real (el pago se **registra**, no se procesa), mapa dibujado/interactivo con gráficos complejos (se usa cuadrícula HTML), precios dinámicos, reventa entre usuarios, app móvil nativa, envío masivo de correo en producción, integración con redes sociales.

### 1.5 Definiciones
| Término | Significado |
|---|---|
| Evento | Espectáculo con fecha, recinto y tipo: concierto, partido, ópera, teatro u otro. |
| Recinto | Lugar físico (estadio, arena, teatro) con zonas y asientos. |
| Zona | Sección del recinto (Platea, Palco, Gradas, General…) que agrupa asientos y tiene precio por evento. |
| Asiento | Ubicación individual (zona, fila, número). **Un asiento = un boleto.** |
| Cliente | Usuario con cuenta propia (rol Cliente) que compra y administra sus boletos. |
| Venta | Transacción de un cliente que agrupa uno o más boletos (canal Web o Mostrador). |
| Boleto (Entrada) | Documento digital con código único/QR asociado a un asiento de un evento. |
| Reserva temporal | Bloqueo breve de asientos elegidos mientras el cliente completa la compra. |
| Acceso | Registro de ingreso de un boleto por una puerta. |

---

## 2. Roles del sistema y módulos asignados

El sistema tiene **4 roles**; cada integrante es responsable de **un rol** y de los **módulos** que le corresponden, y los explica de punta a punta (vista, controlador, servicio, DAO y base de datos).

### 2.1 Resumen

| Integrante | Rol | Módulos a cargo | Qué hace en el sistema |
|---|---|---|---|
| 1 | **Organizador** (productor) | **M1** Eventos y cartelera · **M2** Recintos y asientos | Crea y publica eventos con su descripción y participantes, fija precios por zona, define recintos con sus asientos y evita choques de horario en el recinto. También consulta los reportes (M7). |
| 2 | **Cliente** (usuario final) | **M3** Portal del cliente: cartelera, asientos, compra y mis boletos | Se registra, explora eventos, elige asientos en el mapa, los reserva y compra, recibe código/QR, consulta su historial y anula sus compras. |
| 3 | **Control de acceso** (puerta y seguridad) | **M4** Control de acceso · **M5** Incidencias | Valida boletos en puerta, impide reingresos fraudulentos, lleva la ocupación en vivo y registra incidentes. |
| 4 | **Administrador** | **M6** Usuarios y seguridad · **M7** Dashboard y reportes | Gestiona usuarios y roles y la seguridad; consulta dashboard y reportes de todo el sistema. |

> El rol **Cliente** reemplaza al antiguo rol Taquilla: la compra la hace el propio usuario desde el portal. La venta presencial en mostrador queda como función opcional del Administrador (RF-24). La cartelera (RF-15) es pública; todo lo demás del Cliente requiere cuenta.

### 2.2 Matriz de acceso (✔ completo · 👁 solo consulta · ✖ sin acceso)

| Módulo | Organizador | Cliente | Control de acceso | Administrador |
|---|:-:|:-:|:-:|:-:|
| M1 Eventos y cartelera | ✔ | 👁 (publicados) | 👁 | ✔ |
| M2 Recintos y asientos | ✔ | 👁 (mapa de asientos) | 👁 | ✔ |
| M3 Portal del cliente | 👁 | ✔ (solo lo propio) | 👁 (validar) | ✔ (incluye mostrador) |
| M4 Control de acceso | 👁 | ✖ | ✔ | ✔ |
| M5 Incidencias | 👁 | ✖ | ✔ | ✔ |
| M6 Usuarios y seguridad | ✖ | perfil propio | ✖ | ✔ |
| M7 Dashboard y reportes | 👁 (reportes, sin dashboard) | ✖ | ✖ | ✔ |

### 2.3 Flujo entre roles

```
Organizador ──publica evento, recinto con asientos, y precios──▶ Cliente ──elige asiento, reserva y compra (QR)──▶
Control de acceso ──valida en puerta, registra incidencias──▶ Administrador ──reportes
```

### 2.4 Qué explica cada integrante en la exposición

| Rol | Puntos técnicos clave |
|---|---|
| Organizador | CRUD con validaciones, jerarquía recinto–zona–asiento (generación masiva de asientos), **choque de horario por recinto (RN-05)**, estados del evento y publicación. |
| Cliente | Registro/login de usuario final, **mapa de asientos y concurrencia** (dos clientes no pueden tomar el mismo asiento), reserva temporal con expiración, QR, "mis boletos" con control de propiedad, anulación. |
| Control de acceso | Validación de un solo uso, manejo de excepciones (boleto inválido/usado), ocupación en tiempo real, bitácora de incidentes. |
| Administrador | **Sesiones, cookies, filtros de autorización**, hash de contraseñas, consultas agregadas, reportes y exportación. |

---

## 3. Requerimientos funcionales

**Prioridad:** A = alta (núcleo), M = media, B = baja (mejora).

### M1 — Eventos y cartelera *(Organizador)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-01 | Crear, editar, consultar y desactivar eventos (nombre, tipo, descripción, participantes —artista, elenco o equipos, en texto—, afiche opcional como imagen adjunta (JPG, PNG, WEBP o GIF de hasta 2 MB), categoría, recinto, apertura de puertas, inicio, fin, estado). | A |
| RF-02 | Gestionar estados: *Borrador → Publicado (venta abierta) → Agotado → En curso → Finalizado → Cancelado*. | A |
| RF-03 | Listar eventos con búsqueda por nombre y filtros por tipo, fecha, categoría, recinto y estado, con paginación. | A |
| RF-04 | Mantener catálogo de categorías (rock, pop, ópera, fútbol, básquetbol, teatro…). | M |
| RF-05 | Definir el precio de cada zona del recinto para cada evento. | A |
| RF-06 | Validar: fin posterior al inicio, apertura de puertas anterior al inicio, precios ≥ 0, **recinto sin otro evento en el mismo horario (RN-05)** y que **todas las zonas tengan precio** antes de publicar. | A |
| RF-07 | Al publicar, generar la disponibilidad de asientos del evento (todos los asientos activos del recinto en estado *Disponible*). | A |
| RF-08 | Bloquear asientos de un evento (cortesías, producción, visibilidad reducida). | M |
| RF-09 | Duplicar un evento como plantilla (precios por zona incluidos). | B |

### M2 — Recintos y asientos *(Organizador)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-10 | CRUD de recintos (nombre, dirección, tipo: estadio, arena, teatro). | A |
| RF-11 | CRUD de zonas de un recinto (nombre, descripción). | A |
| RF-12 | Generar los asientos de una zona indicando filas y número de asientos por fila (p. ej. filas A–J × 20), y editar o inhabilitar asientos sueltos. | A |

### M3 — Portal del cliente: cartelera, asientos, compra y mis boletos *(Cliente)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-13 | Registro de cuenta del cliente (nombres, documento, correo, teléfono, contraseña): crea un usuario con rol Cliente; edición de su perfil. | A |
| RF-14 | Impedir cuentas duplicadas por documento o correo. | A |
| RF-15 | Cartelera pública: eventos publicados con búsqueda y filtros (tipo, fecha, categoría, recinto); detalle con descripción, participantes, zonas, precios y disponibilidad por zona. | A |
| RF-16 | **Mapa de asientos** del evento (cuadrícula HTML por zona) con estados: disponible, reservado, vendido, bloqueado. | A |
| RF-17 | Seleccionar asientos y comprarlos en una transacción que verifica que sigan disponibles; si alguno se perdió, informar cuál y no cobrar nada. | A |
| RF-18 | **Reserva temporal:** al elegir asientos quedan reservados N minutos (configurable); si vence, se liberan automáticamente. | M |
| RF-19 | Limitar la cantidad de asientos por compra (configurable, p. ej. 6). | A |
| RF-20 | Generar por cada boleto un código único y su QR. | A |
| RF-21 | Registrar forma de pago (tarjeta, transferencia, efectivo) y total — **sin procesar el cobro**. | A |
| RF-22 | "Mis boletos": historial de compras, estado de cada boleto (Emitido/Usado/Anulado), asiento, ver/descargar QR y comprobante en PDF. | A |
| RF-23 | Anular una compra propia dentro del plazo permitido (RN-12), liberando los asientos (reembolso simulado, con motivo). | A |
| RF-24 | Venta en mostrador: el Administrador registra una venta a nombre de un cliente (canal *Mostrador*) eligiendo asientos en el mismo mapa. | M |

### M4 — Control de acceso *(Control de acceso)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-25 | Validar un boleto por código/QR o por documento del cliente. | A |
| RF-26 | Rechazar boletos inexistentes, anulados, ya usados o de otro evento, indicando el motivo. | A |
| RF-27 | Registrar cada ingreso (fecha/hora, puerta, usuario que valida, resultado) y mostrar zona, fila y asiento. | A |
| RF-28 | Mostrar ocupación en tiempo real: ingresados vs. vendidos por zona. | M |
| RF-29 | Anular un ingreso registrado por error, con motivo (queda guardado). | M |
| RF-30 | Permitir validar solo desde la apertura de puertas hasta el fin del evento. | M |
| RF-31 | Re-entrada opcional con regla configurable. | B |

### M5 — Incidencias *(Control de acceso)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-32 | Registrar incidencias (tipo: seguridad, médica, acceso denegado, asiento en disputa, objeto perdido, otro; descripción, gravedad, puerta, hora). | A |
| RF-33 | Dar seguimiento por estado: *Abierta → En atención → Cerrada* con observaciones. | M |
| RF-34 | Consultar incidencias por evento, tipo y gravedad. | M |
| RF-35 | Registrar automáticamente como incidencia de "acceso denegado" los rechazos de RF-26 (opcional por configuración). | B |

### M6 — Usuarios y seguridad *(Administrador)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-36 | Inicio y cierre de sesión con usuario/contraseña. | A |
| RF-37 | CRUD de usuarios (personal interno) y asignación de uno de los 4 roles; las cuentas de Cliente se crean por autorregistro (RF-13) y el Administrador solo las consulta/desactiva. | A |
| RF-38 | Control de acceso por rol a páginas y acciones (filtro de autorización, según la matriz 2.2). | A |
| RF-39 | Contraseñas con hash + salt (BCrypt), nunca en texto plano. | A |
| RF-40 | Bloqueo temporal tras N intentos fallidos de login. | M |
| RF-41 | Cookie "recordar usuario" opcional y expiración de sesión por inactividad. | B |

### M7 — Dashboard y reportes *(Administrador; el Organizador consulta los reportes RF-43 a RF-49)*
| ID | Requerimiento | Prior. |
|---|---|---|
| RF-42 | Dashboard: eventos activos, boletos vendidos, ingresos, ocupación por zona, asistencia. | M |
| RF-43 | Reporte de ventas por evento, zona, canal (Web/Mostrador) y vendedor. | A |
| RF-44 | Reporte de asistencia: vendidos vs. ingresados y porcentaje de ausentismo. | A |
| RF-45 | Reporte de ocupación de asientos por zona y evento. | M |
| RF-46 | Reporte de incidencias por evento, tipo y gravedad. | M |
| RF-47 | Exportar reportes a PDF (se abre en una pestaña nueva del navegador) y Excel/CSV (descarga). | M |
| RF-48 | Filtros por rango de fechas, evento, tipo, categoría y zona. | M |
| RF-49 | Reporte general por evento: sin evento elegido, resumen de todos (asientos, vendidos, sin vender, ocupación, ingresos, asistencia e incidencias); con un evento, toda su información (datos del evento, asientos por estado, ventas e ingresos por canal, zonas con precio y vendidos, asistencia e incidencias). | M |

---

## 4. Reglas de negocio

| ID | Regla |
|---|---|
| RN-01 | **Un asiento = un boleto:** un asiento de un evento solo puede estar *Vendido* a un boleto activo. |
| RN-02 | Un asiento *Reservado* no puede ser elegido por otro cliente hasta que la reserva venza (N minutos) o se cancele; al vencer vuelve a *Disponible*. |
| RN-03 | Solo se venden boletos de eventos *Publicados* y antes de su hora de inicio. |
| RN-04 | Una compra no puede superar el máximo de asientos por compra. |
| RN-05 | Un recinto no puede tener dos eventos activos (no cancelados) con horarios traslapados. |
| RN-06 | Un evento con boletos vendidos no puede cambiar de recinto ni de fechas sin antes anular sus boletos. |
| RN-07 | Un boleto permite **un solo ingreso** (salvo re-entrada habilitada) y solo en el evento y asiento para los que se emitió. |
| RN-08 | Boletos anulados no ingresan; anular un boleto ya usado no está permitido. |
| RN-09 | Un evento *Cancelado* o *Finalizado* no admite ventas ni cambios en sus datos; al cancelar, se anulan sus boletos (reembolso simulado). |
| RN-10 | Los eventos con ventas no se eliminan físicamente; se cancelan o desactivan (borrado lógico). |
| RN-11 | Cuando no quedan asientos *Disponibles* ni *Reservados*, el evento pasa a *Agotado*; si se libera algún asiento, vuelve a *Publicado*. |
| RN-12 | El cliente solo puede anular compras propias, hasta N horas antes de la apertura de puertas (configurable) y si ningún boleto fue usado. |
| RN-13 | Un cliente solo puede ver y descargar sus propios boletos y ventas. |
| RN-14 | Un asiento inhabilitado o bloqueado no se vende; no se pueden eliminar asientos de un recinto con boletos vendidos (solo inhabilitarlos). |

---

## 5. Requerimientos de datos (modelo preliminar)

### 5.1 Entidades
| Entidad | Módulo | Atributos clave |
|---|---|---|
| **Evento** | M1 | id, nombre, tipo (Concierto/Partido/Ópera/Teatro/Otro), descripcion, participantes, afiche (imagen adjunta), afiche_tipo, id_categoria, id_recinto, apertura_puertas, fecha_inicio, fecha_fin, estado, activo |
| **Categoria** | M1 | id, nombre |
| **PrecioZona** | M1 | id, id_evento, id_zona, precio — `UNIQUE (id_evento, id_zona)` |
| **EventoAsiento** | M1 | id, id_evento, id_asiento, estado (Disponible/Reservado/Vendido/Bloqueado), reservado_hasta, id_cliente_reserva — `UNIQUE (id_evento, id_asiento)` |
| **Recinto** | M2 | id, nombre, direccion, tipo |
| **Zona** | M2 | id, id_recinto, nombre, descripcion |
| **Asiento** | M2 | id, id_zona, fila, numero, activo — `UNIQUE (id_zona, fila, numero)` |
| **Cliente** | M3 | id, id_usuario (único), nombres, apellidos, documento (único), correo (único), telefono |
| **Venta** | M3 | id, id_cliente, canal (Web/Mostrador), id_usuario_vendedor (solo mostrador), fecha, total, forma_pago, estado |
| **Entrada** (boleto) | M3 | id, id_venta, id_evento_asiento, precio, codigo (único), estado (Emitida/Usada/Anulada) |
| **Acceso** | M4 | id, id_entrada, fecha_hora, puerta, id_usuario, resultado, anulado, motivo_anulacion |
| **Incidencia** | M5 | id, id_evento, tipo, descripcion, gravedad, puerta, estado, fecha_hora, id_usuario |
| **Usuario** | M6 | id, username (único), password_hash, nombre, id_rol, activo, intentos_fallidos |
| **Rol** | M6 | id, nombre (Organizador, Cliente, Control de acceso, Administrador) |

### 5.2 Relaciones principales
- Categoría 1—N Evento · Recinto 1—N Evento
- Recinto 1—N Zona 1—N Asiento
- Evento 1—N PrecioZona N—1 Zona · Evento 1—N EventoAsiento N—1 Asiento
- Usuario 1—1 Cliente · Cliente 1—N Venta 1—N Entrada N—1 EventoAsiento
- Entrada 1—N Acceso (1 válido, más rechazos si se registran)
- Evento 1—N Incidencia · Rol 1—N Usuario

### 5.3 Restricciones y estrategia de concurrencia
- Claves foráneas con integridad referencial; `UNIQUE (codigo)` en entrada; índices por fecha, evento y llaves foráneas; InnoDB, `utf8mb4`.
- **Un asiento por evento** vive en una sola fila de `EventoAsiento`; su `estado` es la fuente de verdad.
- Reservar/comprar un asiento se hace con una actualización condicional dentro de una transacción, verificando las filas afectadas:
  `UPDATE evento_asiento SET estado='Reservado', reservado_hasta=?, id_cliente_reserva=? WHERE id=? AND (estado='Disponible' OR (estado='Reservado' AND reservado_hasta < NOW()))`
  Si no se actualiza la fila, el asiento ya no está disponible y se informa al cliente.
- Las reservas vencidas se liberan al consultar el mapa o por una tarea programada (`ServletContextListener`), sin depender de un proceso externo.

---

## 6. Requerimientos no funcionales

| ID | Categoría | Requerimiento |
|---|---|---|
| RNF-01 | Plataforma | Java 11+, Maven, WAR, Apache Tomcat 9 con `javax.servlet.*` (**nunca** `jakarta`). |
| RNF-02 | Base de datos | MySQL 8, JDBC con DAO y `PreparedStatement`. |
| RNF-03 | Arquitectura | MVC en capas: `controller`, `model`, `dao`, `service`, `filter`, `util`; vistas en `webapp`. |
| RNF-04 | Seguridad | Consultas parametrizadas (SQL Injection), escape de salida (XSS, JSTL `c:out`), filtros de autorización, token CSRF en formularios, QR/códigos no predecibles, control de propiedad (un cliente no accede a boletos ajenos cambiando un id en la URL), validación de imágenes adjuntas por su firma de bytes y tamaño máximo. |
| RNF-05 | Concurrencia | Dos clientes nunca obtienen el mismo asiento en el mismo evento; un mismo código no ingresa dos veces. Verificable con pruebas de compra simultánea. |
| RNF-06 | Configuración | Credenciales fuera del repositorio (`db.properties.example` versionado, el real en `.gitignore`); minutos de reserva, máximo por compra y horas de anulación configurables. |
| RNF-07 | Usabilidad | Interfaz responsive; mapa de asientos legible en celular; pantalla de **validación en puerta** usable desde celular/tablet, con resultado claro (válido/rechazado). |
| RNF-08 | Rendimiento | Listados paginados; mapa de asientos de hasta ~2 000 asientos cargado en < 2 s; validación de boleto < 1 s. |
| RNF-09 | Reproducibilidad | Otro integrante levanta el proyecto solo con el README (incluye `schema.sql` y datos de prueba: un usuario por rol, un recinto con asientos y un evento publicado). |
| RNF-10 | Mantenibilidad | Convenciones de nombres, Javadoc en clases públicas, sin código duplicado, commits descriptivos. |
| RNF-11 | Manejo de errores | Excepciones controladas, páginas de error (400/403/404/500), registro con `java.util.logging` o SLF4J. |
| RNF-12 | Compatibilidad | Chrome, Edge y Firefox actuales. |
| RNF-13 | Codificación | UTF-8 en JSP, filtros y conexión a BD. |
| RNF-14 | Control de versiones | Git/GitHub, ramas por rol/funcionalidad, aportes visibles de los 4 integrantes. |
| RNF-15 | Dependencias | Generación de QR con ZXing y reportes con una librería compatible con Tomcat 9 / `javax`. |

---

## 7. Casos de uso principales

| ID | Caso de uso | Rol | Resultado |
|---|---|---|---|
| CU-01 | Iniciar sesión | Todos | Sesión creada con permisos del rol. |
| CU-02 | Crear evento, fijar precios y publicar | Organizador | Evento visible con asientos disponibles. |
| CU-03 | Definir recinto con zonas y asientos | Organizador | Mapa de asientos listo para usar. |
| CU-04 | Editar, bloquear asientos o cancelar un evento | Organizador | Evento actualizado; si se cancela, sus boletos se anulan. |
| CU-05 | Registrarse y consultar cartelera | Cliente | Cuenta creada; eventos visibles. |
| CU-06 | Elegir asientos y comprar boletos | Cliente | Compra con boletos QR; asientos vendidos. |
| CU-07 | Anular compra | Cliente | Asientos liberados; motivo guardado. |
| CU-08 | Validar boleto en puerta | Control de acceso | Ingreso registrado una sola vez. |
| CU-09 | Registrar y dar seguimiento a incidencia | Control de acceso | Incidencia trazable hasta su cierre. |
| CU-10 | Administrar usuarios y roles | Administrador | Usuario creado o modificado. |
| CU-11 | Consultar dashboard y reportes | Administrador | Indicadores y exportación. |

### Detalle — CU-06 Elegir asientos y comprar boletos
- **Precondición:** cliente autenticado; evento *Publicado* con asientos disponibles.
- **Flujo principal:** 1) Elegir evento en la cartelera. 2) El sistema muestra el mapa de asientos por zona con precios. 3) El cliente selecciona asientos (hasta el máximo por compra). 4) El sistema los reserva temporalmente (RN-02) y muestra el tiempo restante. 5) El cliente confirma y registra la forma de pago. 6) En una transacción se pasan los asientos a *Vendido*, se crea la venta y se generan los boletos con código/QR. 7) Se muestra el comprobante y los boletos quedan en "Mis boletos".
- **Alternos:** un asiento ya fue tomado por otro cliente → se informa cuál y se pide elegir otro; la reserva vence → asientos liberados y se vuelve al mapa; evento no publicado → bloqueo; evento sin asientos → estado *Agotado*.
- **Postcondición:** asientos *Vendidos*; si no quedan disponibles, el evento pasa a *Agotado* (RN-11).

### Detalle — CU-08 Validar boleto en puerta
- **Precondición:** usuario Control de acceso autenticado; evento con puertas abiertas.
- **Flujo principal:** 1) Escanear/escribir código. 2) El sistema verifica existencia, estado, evento y que no esté usado (RN-07, 08). 3) Marca el boleto como *Usado* y registra el acceso. 4) Muestra "VÁLIDO" con zona, fila y asiento.
- **Alternos:** código inexistente, anulado o ya usado → "RECHAZADO" con motivo y opción de registrar incidencia.

---

## 8. Arquitectura del proyecto

```
TICKA/                               (repositorio github.com/Jozeeh/TICKA)
├── pom.xml                          (WAR, Java 11, Jakarta EE 8 = javax.*)
├── README.md
├── docs/
│   ├── requerimientos.md            ← este documento
│   ├── diagrama_er.md
│   └── guion_demo.md
├── sql/
│   ├── 00_usuario_aplicacion.sql    (usuario MySQL con permisos limitados)
│   ├── 01_esquema_ticka.sql         (tablas, vista y catálogos)
│   ├── 02_datos_prueba.sql          (recintos, eventos, ventas y accesos de ejemplo)
│   ├── 03_afiche_evento.sql         (migración: afiche como imagen adjunta, solo bases anteriores)
│   └── 04_quitar_aviso_disponibilidad.sql (migración: retira el aviso de disponibilidad, solo bases anteriores)
└── src/
    ├── main/java/sv/edu/itca/ticka/
    │   ├── modelo/                  (Evento, Recinto, Zona, Asiento, Usuario, Rol...)
    │   ├── dao/  y  dao/impl/       (interfaces DAO e implementaciones JDBC)
    │   ├── servicio/                (reglas de negocio y transacciones)
    │   ├── controlador/             (inicio/cartelera, afiche, acceso y panel)
    │   │   ├── organizador/         (eventos, recintos, zonas, categorías, mapa)
    │   │   └── admin/               (dashboard, usuarios, clientes, reportes)
    │   ├── filtro/                  (Codificación, CSRF, Autenticación y rol)
    │   ├── persistencia/            (ConexionBD)
    │   └── util/                    (Parámetros, Formatos, Sesión, CSV)
    ├── main/resources/              (db.properties.example; db.properties local, ignorado por Git)
    ├── main/webapp/
    │   ├── index.jsp
    │   ├── resources/css/estilos.css
    │   └── WEB-INF/ (web.xml, vistas/{plantilla,acceso,panel,organizador,admin,error})
    └── test/java/                   (pruebas JUnit de las reglas de negocio)
```

Las vistas están organizadas por rol (`vistas/organizador`, `vistas/admin`, y luego `vistas/cliente` y `vistas/acceso`), así cada integrante trabaja en su carpeta, se reducen los conflictos en Git y se explica con claridad.

**Flujo:** Navegador → Filtros (UTF-8, CSRF, sesión y rol) → Servlet → Servicio (reglas) → DAO → MySQL → JSP/JSTL (o JSF/PrimeFaces en una etapa posterior).

**Tecnologías:** JSP + JSTL y Servlets en la etapa base; JSF + PrimeFaces + AJAX para tablas dinámicas, mapa de asientos con refresco, programa y dashboard; ZXing para QR; OpenPDF (derivado libre de iText) para los reportes en PDF y CSV con BOM para Excel.

---

## 9. Cobertura de temas del ciclo

| Tema de la guía | Dónde se aplica | Rol |
|---|---|---|
| Formularios y validación | Evento, precios, registro de cliente, compra (RF-01, RF-06, RF-13, RF-17) | Organizador / Cliente |
| Control de flujo y excepciones | Choque de horario en el recinto, asiento ya tomado, boleto inválido (RF-06, RF-17, RF-26) | Organizador / Cliente / Acceso |
| Sesiones y cookies | Login, rol en sesión, reserva ligada al usuario, "recordar usuario" (RF-36, RF-18, RF-41) | Administrador / Cliente |
| POO y MVC | Entidades, servicios y controladores separados (RNF-03) | Todos |
| Persistencia JDBC/DAO | Todas las entidades; transacciones en compra, reserva y anulación (RNF-05) | Todos |
| JSF, PrimeFaces, AJAX | Listados con filtro, mapa de asientos con refresco, ocupación en vivo, dashboard (RF-03, RF-16, RF-28, RF-42) | Todos |
| Seguridad | Hash, filtros de rol, CSRF, control de propiedad (RF-38, RF-39, RF-40, RNF-04) | Administrador |
| Reportería | General por evento, ventas, asistencia, ocupación, incidencias (RF-43 a RF-49) | Administrador / Organizador |

---

## 10. Plan de entrega sugerido (semanas 11–16)

> Ajustar según lo ya implementado en las semanas 1–10. Todos trabajan en paralelo sobre su rol; la BD base se define en conjunto la semana 11.

| Semana | Común | Organizador (M1, M2) | Cliente (M3) | Control de acceso (M4, M5) | Administrador (M6, M7) |
|---|---|---|---|---|---|
| **11** | `schema.sql` completo y estructura Maven/Git | CRUD eventos, categorías, recintos y zonas | Registro de cliente y cartelera pública | Maqueta de pantalla de validación | Login básico, usuarios y roles |
| **12** | — | Generación de asientos, precios por zona, publicación (RF-07) | Mapa de asientos y compra con actualización condicional | Validación de código y registro de acceso | Filtros de autorización por rol, hash |
| **13** | Integración de flujo compra → acceso | Regla de choque de horario por recinto, bloqueo de asientos | Reserva temporal, mis boletos, QR, anulación | Rechazos con motivo, ocupación en vivo, incidencias | Bloqueo por intentos fallidos, expiración de sesión |
| **14** | Migración/mejora de vistas a JSF + PrimeFaces | Listado de eventos con AJAX | Mapa de asientos con AJAX | Ocupación en vivo con AJAX | Dashboard |
| **15** | — | Duplicar evento, pulido de validaciones | Venta en mostrador | Re-entrada, anulación de ingreso | Reportes y exportación PDF/Excel |
| **16** | Pruebas cruzadas (incluida compra simultánea), documentación final, despliegue y presentación | | | | |

---

## 11. Criterios de aceptación globales

1. El proyecto compila con `mvn clean package` y el WAR se despliega en Tomcat 9 sin errores.
2. Con solo el README, otro integrante crea la BD, configura credenciales y entra a `/ticka` con un usuario de cada rol.
3. No hay credenciales en el repositorio.
4. Se verifican y documentan las reglas RN-01 a RN-14 con pruebas manuales (asiento ya vendido, reserva vencida, límite por compra, doble ingreso con el mismo QR, boleto anulado, choque de horario en el mismo recinto).
5. Dos clientes que intentan comprar el mismo asiento a la vez: solo uno lo obtiene y el otro recibe un aviso claro.
6. Cada rol solo accede a lo que indica la matriz 2.2; lo demás responde 403 o redirige al login.
7. Consultas solo con `PreparedStatement`; contraseñas con hash.
8. El flujo completo funciona: Organizador publica → Cliente elige asiento y compra → Acceso valida → Administrador ve el reporte.
9. El historial de Git muestra aportes de los 4 integrantes, y cada uno puede explicar su rol y módulos.

---

## 12. Riesgos y supuestos

| Riesgo / Supuesto | Mitigación |
|---|---|
| Alcance excesivo para el tiempo restante. | Priorizar A > M > B; primero el flujo mínimo "mapa → compra → validación", extras al final. |
| El mapa de asientos y la concurrencia concentran la complejidad en el rol Cliente. | Usar cuadrícula HTML simple; resolver la concurrencia con una actualización condicional; la reserva temporal (M) puede entrar en la semana 13. |
| Dependencia entre roles (Acceso necesita boletos del Cliente; el Cliente necesita asientos del Organizador). | Fijar el esquema de BD común en la semana 11 y cargar datos de prueba compartidos (recinto con asientos y evento publicado). |
| Liberar reservas vencidas sin un servicio externo. | Evaluar el vencimiento al consultar el mapa y, opcionalmente, con un `ServletContextListener` programado. |
| Carga desigual entre roles. | Revisar semanalmente; reasignar RF de prioridad B/M (p. ej. RF-09, RF-24) si hace falta. |
| Confusión `javax` / `jakarta` al agregar librerías. | Validar compatibilidad con Tomcat 9 antes de agregar dependencias. |
| JSF/PrimeFaces no compatibles con Tomcat 9. | Usar variantes `javax` (Mojarra 2.3.x, PrimeFaces 11–12) y probar temprano. |
| Conflictos de integración en Git. | Ramas por rol/funcionalidad y vistas separadas por carpeta de rol. |
| Se supone MySQL local para todos. | `schema.sql` y datos de prueba en `docs/sql`. |

---

## 13. Pendientes de decisión del equipo

- [ ] Confirmar nombre definitivo (TICKA) y ruta de contexto.
- [ ] Asignar cada rol a un integrante y registrarlo en la bitácora.
- [ ] Decidir cuántos minutos dura la reserva temporal y cuántos asientos se permiten por compra.
- [ ] Decidir si entra la venta en mostrador (RF-24).
- [ ] Decidir si habrá re-entrada (RF-31).
- [x] Librería de reportes: OpenPDF para PDF y CSV para Excel (RF-47).
