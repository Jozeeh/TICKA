# Guion de la demostración — Avance TICKA

Proyecto completo: **base de datos y los 4 roles** (Administrador, Organizador, Cliente y Control de acceso). Duración sugerida: 25 a 30 minutos; cada integrante presenta su rol.

## Preparación (antes de la clase)

1. Ejecutar de nuevo `sql/01_esquema_ticka.sql` y `sql/02_datos_prueba.sql` para tener los datos limpios. Las fechas de los eventos se calculan desde el día en que se ejecuta el script.
2. Iniciar la aplicación en Tomcat 9 y abrir la página de inicio: deben aparecer los eventos en cartelera (si no aparecen, revisar MySQL y `db.properties`).
3. Cuentas de prueba (contraseña `itca1234`): `admin`, `organizador`, `puerta` y `cliente`.
4. En la sección del Cliente se puede registrar además una cuenta nueva en vivo desde **Crear cuenta**.
5. Para la puerta: editar un evento publicado para que su apertura de puertas sea antes de la hora de la demo (solo se valida entre la apertura y el fin, RF-30).
6. Tener abiertas varias ventanas (normal e incógnito) para cambiar de rol rápido.
7. Tener a mano `docs/diagrama_er.md` (en GitHub se ven los diagramas).

---

## 1. Base de datos (3 min)

- **Mostrar** el diagrama general de `docs/diagrama_er.md`, con sus colores por módulo.
- **Explicar:**
  - Recinto → zona → asiento es la *plantilla* del lugar.
  - `evento_asiento` es la copia de esos asientos para un evento, con su estado. La restricción `UNIQUE (id_evento, id_asiento)` garantiza **un asiento = un boleto** (RN-01).
  - `entrada` es el boleto, con un código único para el QR; `acceso` registra cada escaneo en puerta.
  - La vista `v_ocupacion_evento_zona` alimenta el dashboard y los reportes.
- **Seguridad:** la aplicación usa el usuario MySQL `ticka_app`, con permisos limitados, y sus credenciales están en `src/main/resources/db.properties`, un archivo local tipo `.env` que Git ignora (en el repositorio solo va `db.properties.example`).

## 2. Seguridad y Administrador (5 a 6 min)

> Las opciones de cada rol están en el **menú lateral** izquierdo; la barra superior solo tiene la marca, la cartelera y la sesión.

1. **Inicio de sesión:**
   - Escribir mal la contraseña: aparece "Le quedan N intentos" (bloqueo de 15 min al quinto intento, RF-40).
   - Entrar bien y marcar "Recordar mi usuario" (cookie, RF-41).
   - Explicar que la contraseña está con **BCrypt** y que todos los formularios llevan **token CSRF**.
2. **Dashboard** (`/admin/dashboard`): boletos vendidos, ingresos, ocupación de eventos activos, asistencia del evento finalizado (Orquesta Salvadoreña: 79 %) e incidencias abiertas. Debajo, los **gráficos** (RF-53): ingresos por mes, por canal, eventos con más ingresos y ocupación de los próximos eventos.
3. **Usuarios:**
   - Crear un usuario con contraseña débil para mostrar la validación; luego crearlo bien.
   - Intentar desactivar la propia cuenta: el sistema lo impide (siempre queda un administrador activo).
   - Si hay una cuenta bloqueada, mostrar el botón **Desbloquear**.
4. **Clientes:** buscar un cliente (los listados de Usuarios y Clientes tienen búsqueda y paginación; Usuarios además filtra por rol, RF-55) y pulsar **Ver** en *Ana López*: aparecen sus datos y cada compra con sus boletos (RF-54); desactivar y volver a activar su cuenta.
5. **Reportes:**
   - Pestañas de ventas (por zona y canal Web/Mostrador), asistencia (vendidos frente a ingresados, ausentes y rechazos en puerta), ocupación e incidencias.
   - La pestaña de ventas incluye el **vendedor** (las ventas de mostrador muestran quién las registró).
   - Pestaña **General** (RF-49): sin evento muestra el resumen de todos; al elegir *Maná* aparece toda su información (asientos vendidos y sin vender, ingresos por canal, zonas, asistencia e incidencias).
   - Filtrar por tipo, categoría o zona (RF-48) y **Exportar PDF**: se abre en una pestaña nueva con formato formal (datos del informe, resumen, detalle y "Página X de Y"). Luego **Exportar CSV** y abrirlo en Excel para mostrar que las tildes se ven bien.
   - Con la cuenta del Organizador, abrir **Reportes** en su menú lateral: también los consulta, pero `/admin/dashboard` responde 403.
6. **Autorización:** en la ventana del Organizador, abrir `/ticka/admin/dashboard`: responde **403 Acceso denegado**.

## 3. Organizador (6 a 7 min)

1. **Recintos → Zonas y asientos** (Teatro Presidente):
   - Mostrar la cuadrícula por zona.
   - Agregar una zona nueva con su distribución (fila A a la C, 8 asientos por fila) en un solo paso. Luego cambiar la distribución (por ejemplo, hasta la fila D) y pulsar **Guardar zona**: crea los que faltan e inactiva, sin borrar, los que quedan fuera.
   - Hacer clic en un asiento para inhabilitarlo (RN-14: no se borra).
2. **Calendario** (`/organizador/calendario`, RF-50): navegar al mes siguiente, filtrar por recinto y marcar **Solo mis eventos** (RF-52; los eventos de los datos de prueba no tienen creador, así que aparecen los que se creen durante la demo).
   - Explicar la **finalización automática** (RF-51): cada minuto una tarea pasa a *En curso* o *Finalizado* los eventos según su horario y libera las reservas vencidas.
3. **Eventos:**
   - Listado con filtros por tipo, estado, recinto, categoría y fechas, y búsqueda por nombre o participantes.
   - Pulsar **Nuevo evento** (por ejemplo, un concierto en el Estadio Cuscatlán en una fecha libre): al elegir el recinto aparecen sus zonas y el precio de cada una se escribe en el mismo formulario. Al guardar queda en **Borrador**.
   - Pulsar **Publicar**: "320 asientos habilitados para la venta" (transacción, RF-07).
   - Pulsar **Duplicar** en un evento (RF-09): se abre el formulario con sus datos, precios y afiche; solo se eligen fechas nuevas y queda en Borrador. En el detalle de esa copia, **Desactivar** la quita del listado (borrado lógico, RN-10).
   - **Editar datos** y adjuntar un afiche (imagen de hasta 2 MB) con su vista previa; volver al **Inicio** para verlo en la tarjeta de la cartelera. Los eventos sin afiche muestran la imagen por defecto.
4. **Mapa de asientos** de *Maná* (o *Ricardo Arjona*, que también tiene asientos reservados):
   - Colores por estado: vendido, reservado, bloqueado y disponible.
   - Bloquear un asiento disponible (cortesía).
   - Intentar bloquear uno vendido: el sistema lo rechaza.
5. **Choque de horario (RN-05):** crear un evento en el Teatro Presidente el mismo día y hora que *Ricardo Arjona*. Se rechaza: "El recinto ya tiene otro evento en ese horario".
6. **Estados y cancelación:**
   - En un evento publicado, mostrar *Marcar como En curso*.
   - Cancelar *Final Alianza vs. FAS*: se anulan sus 22 boletos (reembolso simulado) y se liberan los asientos (RN-09).
   - Volver al **Dashboard** y a **Reportes** como Administrador para ver que los números cambiaron.

## 4. Cliente (6 a 7 min)

1. En la página de inicio, buscar *Maná* con los filtros de la cartelera (RF-15) y abrir **Ver boletos**: detalle público con precios y disponibilidad por zona.
2. Pulsar **Elegir asientos**: pide iniciar sesión; usar **Crear cuenta** (RF-13). Intentar registrar el mismo documento otra vez: se rechaza (RF-14). Al crear la cuenta vuelve al evento.
3. En la pantalla de asientos (RF-16): a la izquierda el evento con zonas, precios y disponibilidad; al elegir una zona a la derecha, el mapa muestra solo los asientos de esa zona. Marcar asientos en el mapa: "Su selección" (abajo a la derecha) muestra los asientos y el total; al pasar de 6 se avisa del máximo (RF-19).
4. **Reservar asientos**: aparece la reserva con cuenta regresiva de 10 minutos (RF-18). En otra ventana, otro cliente intenta el mismo asiento: "Estos asientos ya no están disponibles" (RNF-05).
5. **Pago con tarjeta** (RF-17, RF-21): datos de facturación ya llenos con su cuenta, tarjeta como única forma de pago y vista previa de la tarjeta; cualquier número sirve (la validación es ficticia y no se guarda). Al pulsar **Pagar** aparece "Procesando pago…" y luego **¡Pago aprobado!**. En **Mis boletos** aparece cada boleto con su QR (RF-20) y el **Comprobante PDF** (RF-22).
6. **Anular compra** con motivo (RF-23): los asientos vuelven a estar libres. Explicar el plazo de 24 horas antes de la apertura (RN-12).
7. Como Administrador, **Venta en mostrador** (RF-24): buscar al cliente por documento, elegir asientos y vender; aparece en el detalle del cliente y en el reporte de ventas con canal Mostrador.

## 5. Control de acceso (5 a 6 min)

1. Con la cuenta de Control de acceso, abrir **Validar boletos**, elegir el evento y la puerta.
2. Copiar un código de **Mis boletos** del cliente (o escanear el QR con la cámara del celular) y validar: **VÁLIDO** en verde con zona, fila y asiento (RF-27).
3. Validar el mismo código otra vez: **RECHAZADO — Boleto ya utilizado** (RN-07). Probar un código falso y uno de otro evento (RF-26).
4. **Buscar por documento** (RF-25) y dar ingreso a otro boleto del cliente.
5. En la bitácora, **Anular** un ingreso con motivo (RF-29): el boleto vuelve a quedar disponible.
6. **Ocupación en vivo** (RF-28): validar un boleto en otra ventana y ver que el porcentaje cambia solo en unos segundos (AJAX).
7. **Incidencias**: los rechazos ya crearon incidencias "Acceso denegado" (RF-35). Registrar una incidencia médica, pasarla a *En atención* y cerrarla con observaciones (RF-32, RF-33); filtrar por gravedad (RF-34).

## 6. Cierre (1 min)

- **Arquitectura MVC en capas:** Servlet → Servicio (reglas y transacciones) → DAO (JDBC) → MySQL, con vistas JSP/JSTL.
- **Pruebas:** 58 pruebas JUnit (`mvn test`) de las reglas que no dependen de la base.
- **Flujo completo (criterio 8):** el Organizador publica → el Cliente elige asiento y compra → Control de acceso valida → el Administrador ve el reporte.

## Preguntas frecuentes

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué no `jakarta.*`? | Requisito del módulo: Tomcat 9 usa `javax.servlet.*`. |
| ¿Cómo se evita vender el mismo asiento dos veces? | `UNIQUE (id_evento, id_asiento)` en `evento_asiento`, más una actualización condicional en la compra (portal del cliente). |
| ¿Dónde están las contraseñas? | Con hash BCrypt en `usuario.password_hash`; la de MySQL, fuera del repositorio. |
| ¿Por qué se copia la plantilla de asientos al publicar? | Para que cambiar el recinto después no altere los eventos ya publicados ni sus ventas. |
| ¿Qué pasa si se inhabilita un asiento? | Deja de incluirse en los próximos eventos; los ya publicados conservan su mapa. |
