# Guion de la demostración — Avance TICKA

Avance: **base de datos + rol Administrador + rol Organizador**. Duración sugerida: 15 a 18 minutos.

## Preparación (antes de la clase)

1. Ejecutar de nuevo `sql/01_esquema_ticka.sql` y `sql/02_datos_prueba.sql` para tener los datos limpios. Las fechas de los eventos se calculan desde el día en que se ejecuta el script.
2. Iniciar la aplicación en Tomcat 9 y abrir la página de inicio: deben aparecer los eventos en cartelera (si no aparecen, revisar MySQL y `db.properties`).
3. Entrar a `/acceso`, crear el administrador en **Configuración inicial** y, desde **Usuarios**, crear una cuenta de Organizador.
4. Tener abiertas dos ventanas: una normal (Administrador) y otra de incógnito (Organizador).
5. Tener a mano `docs/diagrama_er.md` (en GitHub se ven los diagramas).

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

1. **Inicio de sesión:**
   - Escribir mal la contraseña: aparece "Le quedan N intentos" (bloqueo de 15 min al quinto intento, RF-41).
   - Entrar bien y marcar "Recordar mi usuario" (cookie, RF-42).
   - Explicar que la contraseña está con **BCrypt** y que todos los formularios llevan **token CSRF**.
2. **Dashboard** (`/admin/dashboard`): boletos vendidos, ingresos, ocupación de eventos activos, asistencia del evento finalizado (La Traviata: 79 %) e incidencias abiertas.
3. **Usuarios:**
   - Crear un usuario con contraseña débil para mostrar la validación; luego crearlo bien.
   - Intentar desactivar la propia cuenta: el sistema lo impide (siempre queda un administrador activo).
   - Si hay una cuenta bloqueada, mostrar el botón **Desbloquear**.
4. **Clientes:** buscar un cliente, ver sus compras y total, y desactivar y volver a activar su cuenta.
5. **Reportes:**
   - Pestañas de ventas (por zona y canal Web/Mostrador), asistencia (vendidos frente a ingresados, ausentes y rechazos en puerta), ocupación e incidencias.
   - Filtrar por evento y **Exportar CSV**; abrirlo en Excel para mostrar que las tildes se ven bien.
6. **Autorización:** en la ventana del Organizador, abrir `/ticka/admin/dashboard`: responde **403 Acceso denegado**.

## 3. Organizador (6 a 7 min)

1. **Recintos → Zonas y asientos** (Teatro Presidente):
   - Mostrar la cuadrícula por zona.
   - Agregar una zona nueva con su distribución (fila A a la C, 8 asientos por fila) en un solo paso. Luego cambiar la distribución (por ejemplo, hasta la fila D) y pulsar **Guardar zona**: crea los que faltan e inactiva, sin borrar, los que quedan fuera.
   - Hacer clic en un asiento para inhabilitarlo (RN-14: no se borra).
2. **Eventos:**
   - Listado con filtros por tipo, estado, recinto y fechas, y búsqueda por nombre o participantes.
   - Abrir **Festival Pop Centroamericano** (Borrador) y pulsar **Publicar** sin precios: aparece "Faltan precios en 3 zonas".
   - Pulsar **Editar datos**: al elegir el recinto aparecen sus zonas y el precio de cada una se escribe en el mismo formulario. Guardar y **Publicar**: "320 asientos habilitados para la venta" (transacción, RF-07).
   - **Editar datos** y adjuntar un afiche (imagen de hasta 2 MB) con su vista previa; volver al **Inicio** para verlo en la tarjeta de la cartelera. Los eventos sin afiche muestran la imagen por defecto.
3. **Mapa de asientos** de *Noche de Rock Salvadoreño*:
   - Colores por estado: vendido, reservado, bloqueado y disponible.
   - Bloquear un asiento disponible (cortesía).
   - Intentar bloquear uno vendido: el sistema lo rechaza.
4. **Choque de horario (RN-05):** crear un evento en el Teatro Presidente el mismo día y hora que *Noche de Rock*. Se rechaza: "El recinto ya tiene otro evento en ese horario".
5. **Estados y cancelación:**
   - En un evento publicado, mostrar *Marcar como En curso*.
   - Cancelar *Final Liga Mayor*: se anulan sus 22 boletos (reembolso simulado) y se liberan los asientos (RN-09).
   - Volver al **Dashboard** y a **Reportes** como Administrador para ver que los números cambiaron.

## 4. Cierre (1 min)

- **Arquitectura MVC en capas:** Servlet → Servicio (reglas y transacciones) → DAO (JDBC) → MySQL, con vistas JSP/JSTL.
- **Pruebas:** 31 pruebas JUnit (`mvn test`) de las reglas que no dependen de la base.
- **Siguiente entrega:** rol Cliente (compra eligiendo asientos en el mismo mapa) y rol Control de acceso (validación del QR en puerta). La base de datos ya los soporta.

## Preguntas frecuentes

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué no `jakarta.*`? | Requisito del módulo: Tomcat 9 usa `javax.servlet.*`. |
| ¿Cómo se evita vender el mismo asiento dos veces? | `UNIQUE (id_evento, id_asiento)` en `evento_asiento`, más una actualización condicional en la compra (portal del cliente). |
| ¿Dónde están las contraseñas? | Con hash BCrypt en `usuario.password_hash`; la de MySQL, fuera del repositorio. |
| ¿Por qué se copia la plantilla de asientos al publicar? | Para que cambiar el recinto después no altere los eventos ya publicados ni sus ventas. |
| ¿Qué pasa si se inhabilita un asiento? | Deja de incluirse en los próximos eventos; los ya publicados conservan su mapa. |
