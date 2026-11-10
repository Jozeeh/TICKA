<%@ page contentType="text/html; charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Acceso denegado" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <section class="card card-body shadow-sm mb-4">
        <h1 class="h3 mb-1">403 · Acceso denegado</h1>
        <p class="text-body-secondary mb-4">No tiene permiso para abrir esta página con su rol actual.</p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/">Volver al inicio</a>
    </section>
</main>
</body>
</html>
