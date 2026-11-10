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
<main class="container-xl py-4">
    <section class="card card-body shadow-sm mb-4">
        <h1 class="h3 mb-1">404 · Página no encontrada</h1>
        <p class="text-body-secondary mb-4">La dirección solicitada no existe o fue movida.</p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/">Volver al inicio</a>
    </section>
</main>
</body>
</html>
