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
<main class="container-xl py-4">
    <h1 class="h3 mb-1">Hola, <c:out value="${u.nombre}" /></h1>
    <p class="text-body-secondary mb-4">Rol: <strong><c:out value="${u.rol.nombre}" /></strong>. Estos son los módulos disponibles para su rol.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-3">
        <c:if test="${u.administrador}">
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/admin/dashboard">
                <h3>Dashboard</h3>
                <p>Indicadores de ventas, ocupación, asistencia e incidencias.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/admin/usuarios">
                <h3>Usuarios y roles</h3>
                <p>Crear cuentas del personal, asignar roles, restablecer contraseñas y desbloquear.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/admin/clientes">
                <h3>Clientes</h3>
                <p>Consultar clientes registrados y sus compras; activar o desactivar cuentas.</p>
            </a></div>
        </c:if>
        <c:if test="${u.gestorEventos}">
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/organizador/eventos">
                <h3>Eventos</h3>
                <p>Crear, duplicar y publicar eventos, fijar precios por zona, cambiar su estado y ver el mapa de asientos.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/organizador/recintos">
                <h3>Recintos, zonas y asientos</h3>
                <p>Registrar recintos, definir sus zonas y generar los asientos numerados por fila.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/organizador/categorias">
                <h3>Categorías</h3>
                <p>Catálogo para clasificar los eventos de la cartelera.</p>
            </a></div>
        </c:if>
        <c:if test="${u.administrador}">
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/reportes">
                <h3>Reportes</h3>
                <p>General por evento, ventas, asistencia, ocupación e incidencias con filtros y exportación a PDF y CSV.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/admin/mostrador">
                <h3>Venta en mostrador</h3>
                <p>Vender boletos a nombre de un cliente eligiendo sus asientos en el mapa.</p>
            </a></div>
        </c:if>
        <c:if test="${u.cliente}">
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/cliente/cartelera">
                <h3>Cartelera</h3>
                <p>Buscar eventos, ver precios por zona y elegir sus asientos en el mapa.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/cliente/boletos">
                <h3>Mis boletos</h3>
                <p>Historial de compras, boletos con código QR, comprobante PDF y anulación.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/cliente/perfil">
                <h3>Mi perfil</h3>
                <p>Editar sus datos y cambiar su contraseña.</p>
            </a></div>
        </c:if>
        <c:if test="${u.personalPuerta}">
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/puerta/validar">
                <h3>Validar boletos</h3>
                <p>Escanear o escribir el código QR; ingreso de un solo uso con el asiento del boleto.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/puerta/ocupacion">
                <h3>Ocupación en vivo</h3>
                <p>Ingresados frente a vendidos por zona, actualizado automáticamente.</p>
            </a></div>
            <div class="col"><a class="card card-body h-100 shadow-sm modulo text-decoration-none" href="${ctx}/puerta/incidencias">
                <h3>Incidencias</h3>
                <p>Registrar incidentes y darles seguimiento hasta cerrarlos.</p>
            </a></div>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
