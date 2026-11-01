<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Panel" />
<c:set var="paginaActiva" value="panel" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <h1>Hola, <c:out value="${u.nombre}" /></h1>
    <p class="subtitulo">Rol: <strong><c:out value="${u.rol.nombre}" /></strong>. Estos son los módulos disponibles para su rol.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="modulos">
        <c:if test="${u.administrador}">
            <a class="tarjeta modulo" href="${ctx}/admin/dashboard" style="text-decoration:none">
                <h3>Dashboard</h3>
                <p>Indicadores de ventas, ocupación, asistencia e incidencias.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/admin/usuarios" style="text-decoration:none">
                <h3>Usuarios y roles</h3>
                <p>Crear cuentas del personal, asignar roles, restablecer contraseñas y desbloquear.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/admin/clientes" style="text-decoration:none">
                <h3>Clientes</h3>
                <p>Consultar clientes registrados y sus compras; activar o desactivar cuentas.</p>
            </a>
        </c:if>
        <c:if test="${u.gestorEventos}">
            <a class="tarjeta modulo" href="${ctx}/organizador/eventos" style="text-decoration:none">
                <h3>Eventos</h3>
                <p>Crear, duplicar y publicar eventos, fijar precios por zona, cambiar su estado y ver el mapa de asientos.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/organizador/recintos" style="text-decoration:none">
                <h3>Recintos, zonas y asientos</h3>
                <p>Registrar recintos, definir sus zonas y generar los asientos numerados por fila.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/organizador/categorias" style="text-decoration:none">
                <h3>Categorías</h3>
                <p>Catálogo para clasificar los eventos de la cartelera.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/reportes" style="text-decoration:none">
                <h3>Reportes</h3>
                <p>General por evento, ventas, asistencia, ocupación e incidencias con filtros y exportación a PDF y CSV.</p>
            </a>
        </c:if>
        <c:if test="${u.administrador}">
            <a class="tarjeta modulo" href="${ctx}/admin/mostrador" style="text-decoration:none">
                <h3>Venta en mostrador</h3>
                <p>Vender boletos a nombre de un cliente eligiendo sus asientos en el mapa.</p>
            </a>
        </c:if>
        <c:if test="${u.cliente}">
            <a class="tarjeta modulo" href="${ctx}/#cartelera" style="text-decoration:none">
                <h3>Cartelera</h3>
                <p>Buscar eventos, ver precios por zona y elegir sus asientos en el mapa.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/cliente/boletos" style="text-decoration:none">
                <h3>Mis boletos</h3>
                <p>Historial de compras, boletos con código QR, comprobante PDF y anulación.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/cliente/perfil" style="text-decoration:none">
                <h3>Mi perfil</h3>
                <p>Editar sus datos y cambiar su contraseña.</p>
            </a>
        </c:if>
        <c:if test="${u.personalPuerta}">
            <a class="tarjeta modulo" href="${ctx}/puerta/validar" style="text-decoration:none">
                <h3>Validar boletos</h3>
                <p>Escanear o escribir el código QR; ingreso de un solo uso con el asiento del boleto.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/puerta/ocupacion" style="text-decoration:none">
                <h3>Ocupación en vivo</h3>
                <p>Ingresados frente a vendidos por zona, actualizado automáticamente.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/puerta/incidencias" style="text-decoration:none">
                <h3>Incidencias</h3>
                <p>Registrar incidentes y darles seguimiento hasta cerrarlos.</p>
            </a>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
