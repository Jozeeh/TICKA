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
    <div class="portada-contenido">
        <div class="portada-texto">
            <span class="sobretitulo">Conciertos · Partidos · Ópera · Teatro</span>
            <h1>Vive los mejores eventos <span>en vivo</span></h1>
            <p>Elige tu evento, escoge tu asiento en el mapa del recinto y entra con tu boleto QR.
                Cada asiento es un boleto: sin sobreventa ni boletos duplicados.</p>
            <div class="acciones-linea">
                <a class="boton acento" href="#cartelera">Ver cartelera</a>
                <c:if test="${empty u}">
                    <a class="boton borde-claro" href="${ctx}/acceso">Iniciar sesión</a>
                </c:if>
            </div>
        </div>
    </div>
</header>

<%-- Indicadores generales --%>
<section class="franja-cifras">
    <div><strong>${empty totalEventos ? 0 : totalEventos}</strong><span>eventos en cartelera</span></div>
    <div><strong>${empty totalDisponibles ? 0 : totalDisponibles}</strong><span>asientos disponibles</span></div>
    <div><strong>${empty totalRecintos ? 0 : totalRecintos}</strong><span>recintos</span></div>
    <div><strong>4</strong><span>tipos de espectáculo</span></div>
</section>

<main class="contenedor">
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Cartelera --%>
    <section id="cartelera" class="seccion">
        <div class="cabecera-pagina">
            <div>
                <h2 class="titulo-seccion">Cartelera</h2>
                <p class="subtitulo">Eventos a la venta y en curso, ordenados por fecha.</p>
            </div>
            <nav class="chips">
                <a class="chip ${empty tipoActivo ? 'activo' : ''}" href="${ctx}/#cartelera">Todos</a>
                <c:forEach var="t" items="${tipos}">
                    <c:if test="${t != 'OTRO'}">
                        <a class="chip ${tipoActivo == t ? 'activo' : ''}" href="${ctx}/?tipo=${t.bd}#cartelera">${t.etiqueta}</a>
                    </c:if>
                </c:forEach>
            </nav>
        </div>

        <div class="cartelera">
            <c:forEach var="ev" items="${eventos}">
                <article class="tarjeta-evento">
                    <div class="evento-recinto"><c:out value="${ev.recinto}" /></div>
                    <%-- Si el evento no tiene afiche (o no carga) se ve la imagen por defecto. --%>
                    <c:choose>
                        <c:when test="${ev.conAfiche}">
                            <div class="cartel" style="background-image:url('${ctx}/afiche?evento=${ev.id}'), url('${ctx}/resources/img/sin-imagen.png')">
                        </c:when>
                        <c:otherwise>
                            <div class="cartel">
                        </c:otherwise>
                    </c:choose>
                        <c:choose>
                            <c:when test="${ev.estado == 'AGOTADO' or ev.disponibles == 0}">
                                <span class="aviso-cartel agotado">Agotado</span>
                            </c:when>
                            <c:when test="${ev.estado == 'EN_CURSO'}">
                                <span class="aviso-cartel en-curso">En curso</span>
                            </c:when>
                            <c:when test="${ev.ultimosAsientos}">
                                <span class="aviso-cartel ultimos">Últimos asientos</span>
                            </c:when>
                        </c:choose>
                    </div>
                    <div class="tarjeta-cuerpo">
                        <h3><c:out value="${ev.nombre}" /></h3>
                        <p class="evento-linea"><c:if test="${not empty ev.participantes}"><c:out value="${ev.participantes}" /> | </c:if>${ev.fechaLarga}</p>
                        <p class="evento-suave">${ev.tipo.etiqueta}<c:if test="${not empty ev.categoria}"> · <c:out value="${ev.categoria}" /></c:if> · ${ev.horarioTexto}</p>
                        <button type="button" class="boton chico" disabled
                                title="La compra en línea llega con el portal del cliente">Ver boletos</button>
                    </div>
                </article>
            </c:forEach>
        </div>
        <c:if test="${empty eventos and empty error}">
            <div class="tarjeta vacio">No hay eventos en cartelera${empty tipoActivo ? '' : ' de este tipo'} por ahora.</div>
        </c:if>
    </section>

    <%-- Cómo funciona --%>
    <section class="seccion">
        <h2 class="titulo-seccion">¿Cómo funciona?</h2>
        <div class="pasos">
            <div class="paso"><span class="numero">1</span><h3>Elige tu evento</h3>
                <p>Explora la cartelera por tipo de espectáculo, fecha y recinto.</p></div>
            <div class="paso"><span class="numero">2</span><h3>Escoge tu asiento</h3>
                <p>Mira el mapa del recinto por zonas y elige exactamente dónde sentarte.</p></div>
            <div class="paso"><span class="numero">3</span><h3>Compra tu boleto</h3>
                <p>Tu asiento queda reservado unos minutos mientras completas la compra.</p></div>
            <div class="paso"><span class="numero">4</span><h3>Entra con tu QR</h3>
                <p>En la puerta se valida tu código una sola vez: nada de boletos duplicados.</p></div>
        </div>
    </section>

    <%-- Roles del sistema --%>
    <section class="seccion">
        <h2 class="titulo-seccion">Una plataforma, cuatro roles</h2>
        <div class="modulos">
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
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
