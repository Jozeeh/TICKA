<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Diagnóstico" />
<c:set var="paginaActiva" value="diagnostico" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <h1>Diagnóstico del entorno</h1>
    <p class="subtitulo">Verifica que Java, Tomcat y MySQL estén listos para ejecutar TICKA.</p>

    <section class="tarjeta">
        <h2>Entorno de ejecución</h2>
        <dl class="datos">
            <c:forEach var="dato" items="${entorno}">
                <dt><c:out value="${dato.key}" /></dt>
                <dd><c:out value="${dato.value}" /></dd>
            </c:forEach>
        </dl>
    </section>

    <section class="tarjeta">
        <h2>Base de datos</h2>
        <c:choose>
            <c:when test="${conexionOk}">
                <div class="alerta exito">Conexión a MySQL correcta.</div>
                <dl class="datos">
                    <c:forEach var="dato" items="${baseDatos}">
                        <dt><c:out value="${dato.key}" /></dt>
                        <dd><c:out value="${dato.value}" /></dd>
                    </c:forEach>
                </dl>
            </c:when>
            <c:otherwise>
                <div class="alerta error">No fue posible conectar: <c:out value="${errorConexion}" /></div>
                <p>Revise que MySQL esté activo, que se hayan ejecutado los scripts de <code>sql/</code>
                    y que exista el archivo <code>db.properties</code> indicado arriba.</p>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
