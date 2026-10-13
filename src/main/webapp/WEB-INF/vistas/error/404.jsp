<%@ page contentType="text/html; charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Página no encontrada" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <section class="tarjeta">
        <h1>404 · Página no encontrada</h1>
        <p class="subtitulo">La dirección solicitada no existe o fue movida.</p>
        <a class="boton" href="${pageContext.request.contextPath}/">Volver al inicio</a>
    </section>
</main>
</body>
</html>
