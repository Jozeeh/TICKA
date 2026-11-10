<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Eventos en vivo" />
<c:set var="paginaActiva" value="inicio" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>

<%-- Portada --%>
<header class="portada-inicio">
    <div class="container-xl portada-contenido">
        <div class="portada-texto">
            <span class="sobretitulo">Conciertos · Partidos · Ópera · Teatro</span>
            <h1>Vive los mejores eventos <span>en vivo</span></h1>
            <p>Elige tu evento, escoge tu asiento en el mapa del recinto y entra con tu boleto QR.
                Cada asiento es un boleto: sin sobreventa ni boletos duplicados.</p>
            <div class="d-flex flex-wrap gap-2 align-items-center">
                <a class="btn btn-acento" href="#cartelera">Ver cartelera</a>
                <c:if test="${empty u}">
                    <a class="btn btn-outline-light" href="${ctx}/acceso">Iniciar sesión</a>
                </c:if>
            </div>
        </div>
    </div>
</header>

<%-- Indicadores generales --%>
<section class="container-xl franja-cifras">
    <div class="row row-cols-2 row-cols-md-4 g-3">
        <div class="col"><div class="card card-body h-100 shadow"><strong>${empty totalEventos ? 0 : totalEventos}</strong><span>eventos en cartelera</span></div></div>
        <div class="col"><div class="card card-body h-100 shadow"><strong>${empty totalDisponibles ? 0 : totalDisponibles}</strong><span>asientos disponibles</span></div></div>
        <div class="col"><div class="card card-body h-100 shadow"><strong>${empty totalRecintos ? 0 : totalRecintos}</strong><span>recintos</span></div></div>
        <div class="col"><div class="card card-body h-100 shadow"><strong>4</strong><span>tipos de espectáculo</span></div></div>
    </div>
</section>

<main class="container-xl py-4">
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Cartelera --%>
    <section id="cartelera" class="mt-4">
        <%-- Rutas de la cartelera en la página de inicio (el fragmento también se usa en el panel del cliente). --%>
        <c:set var="rutaCartelera" value="/" />
        <c:set var="anclaCartelera" value="#cartelera" />
        <c:set var="enlaceTarjeta" value="/evento?id=" />
        <%@ include file="/WEB-INF/vistas/plantilla/cartelera.jspf" %>
    </section>

    <%-- Cómo funciona --%>
    <section class="mt-5">
        <h2 class="h4 mb-1">¿Cómo funciona?</h2>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-xl-4 g-3 mt-1">
            <div class="col"><div class="card card-body h-100 shadow-sm paso"><span class="numero">1</span><h3>Elige tu evento</h3>
                <p>Explora la cartelera por tipo de espectáculo, fecha y recinto.</p></div></div>
            <div class="col"><div class="card card-body h-100 shadow-sm paso"><span class="numero">2</span><h3>Escoge tu asiento</h3>
                <p>Mira el mapa del recinto por zonas y elige exactamente dónde sentarte.</p></div></div>
            <div class="col"><div class="card card-body h-100 shadow-sm paso"><span class="numero">3</span><h3>Compra tu boleto</h3>
                <p>Tu asiento queda reservado unos minutos mientras completas la compra.</p></div></div>
            <div class="col"><div class="card card-body h-100 shadow-sm paso"><span class="numero">4</span><h3>Entra con tu QR</h3>
                <p>En la puerta se valida tu código una sola vez: nada de boletos duplicados.</p></div></div>
        </div>
    </section>

    <%-- Roles del sistema --%>
    <section class="mt-5">
        <h2 class="h4 mb-1">Una plataforma, cuatro roles</h2>
        <div class="row row-cols-1 row-cols-md-2 row-cols-xl-4 g-3 mt-1">
            <div class="col"><article class="card card-body h-100 shadow-sm modulo">
                <h3>Organizador</h3>
                <p>Recintos con zonas y asientos numerados, eventos, precios por zona, publicación y mapa de asientos.</p>
            </article></div>
            <div class="col"><article class="card card-body h-100 shadow-sm modulo">
                <h3>Cliente</h3>
                <p>Cartelera, selección de asientos, compra con reserva temporal y boletos con código QR.</p>
            </article></div>
            <div class="col"><article class="card card-body h-100 shadow-sm modulo">
                <h3>Control de acceso</h3>
                <p>Validación de boletos de un solo uso en puerta, ocupación en vivo e incidencias.</p>
            </article></div>
            <div class="col"><article class="card card-body h-100 shadow-sm modulo">
                <h3>Administrador</h3>
                <p>Usuarios, roles y seguridad; dashboard y reportes de ventas, asistencia y ocupación.</p>
            </article></div>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
