<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Inicio" />
<c:set var="paginaActiva" value="inicio" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>

<header class="portada">
    <h1>TICK<span>A</span></h1>
    <p>Sistema de Eventos en Vivo: conciertos, partidos, ópera y teatro con venta de boletos
        por asiento, control de acceso en puerta e indicadores para la administración.</p>
</header>

<main class="contenedor">
    <section class="modulos">
        <article class="tarjeta modulo">
            <h3>Organizador</h3>
            <p>Recintos con zonas y asientos numerados, eventos, precios por zona, publicación y mapa de asientos.</p>
        </article>
        <article class="tarjeta modulo">
            <h3>Cliente</h3>
            <p>Cartelera, selección de asientos, compra con reserva temporal y boletos con código QR.</p>
        </article>
        <article class="tarjeta modulo">
            <h3>Control de acceso</h3>
            <p>Validación de boletos de un solo uso en puerta, ocupación en vivo e incidencias.</p>
        </article>
        <article class="tarjeta modulo">
            <h3>Administrador</h3>
            <p>Usuarios, roles y seguridad; dashboard y reportes de ventas, asistencia y ocupación.</p>
        </article>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
