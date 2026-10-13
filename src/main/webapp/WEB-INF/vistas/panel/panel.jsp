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
            <a class="tarjeta modulo" href="${ctx}/admin/reportes" style="text-decoration:none">
                <h3>Reportes</h3>
                <p>Ventas, asistencia, ocupación e incidencias con exportación a CSV.</p>
            </a>
        </c:if>
        <c:if test="${u.gestorEventos}">
            <a class="tarjeta modulo" href="${ctx}/organizador/eventos" style="text-decoration:none">
                <h3>Eventos</h3>
                <p>Crear eventos, fijar precios por zona, publicar, cambiar estado y ver el mapa de asientos.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/organizador/recintos" style="text-decoration:none">
                <h3>Recintos, zonas y asientos</h3>
                <p>Registrar recintos, definir sus zonas y generar los asientos numerados por fila.</p>
            </a>
            <a class="tarjeta modulo" href="${ctx}/organizador/categorias" style="text-decoration:none">
                <h3>Categorías</h3>
                <p>Catálogo para clasificar los eventos de la cartelera.</p>
            </a>
        </c:if>
        <c:if test="${u.rol == 'CLIENTE' or u.rol == 'CONTROL_ACCESO'}">
            <article class="tarjeta modulo">
                <h3>Módulo en construcción</h3>
                <p>Las pantallas de este rol forman parte de la siguiente entrega del proyecto.</p>
            </article>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
