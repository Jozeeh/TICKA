<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Cartelera" />
<c:set var="paginaActiva" value="cartelera" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>
    <%-- Misma cartelera que la página de inicio; cada tarjeta lleva a elegir asientos. --%>
    <c:set var="rutaCartelera" value="/cliente/cartelera" />
    <c:set var="anclaCartelera" value="" />
    <c:set var="enlaceTarjeta" value="/cliente/comprar?evento=" />
    <%@ include file="/WEB-INF/vistas/plantilla/cartelera.jspf" %>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
