<%@ page contentType="text/html; charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Error interno" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <section class="tarjeta">
        <h1>500 · Error interno</h1>
        <p class="subtitulo">Ocurrió un error inesperado. Intente de nuevo; si persiste, revise el registro de Tomcat.</p>
        <a class="boton" href="${pageContext.request.contextPath}/">Volver al inicio</a>
    </section>
</main>
</body>
</html>
