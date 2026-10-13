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
