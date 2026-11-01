<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${evento.nombre}" />
<%-- Página pública: se muestra sin menú lateral, como la cartelera. --%>
<c:set var="paginaActiva" value="inicio" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/#cartelera">Cartelera</a> › <c:out value="${evento.nombre}" /></div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="detalle-publico">
        <img class="afiche-grande" alt="Afiche de ${evento.nombre}"
             src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
        <div>
            <span class="etiqueta ${evento.estado.clase}">${evento.estado.bd}</span>
            <h1 style="margin-top:8px"><c:out value="${evento.nombre}" /></h1>
            <p class="evento-linea"><c:out value="${evento.participantes}" /></p>
            <dl class="datos">
                <dt>Fecha</dt><dd>${evento.fechaLarga}</dd>
                <dt>Horario</dt><dd>${evento.horarioTexto} · puertas desde ${evento.aperturaPuertasTexto}</dd>
                <dt>Lugar</dt><dd><c:out value="${evento.recinto}" /></dd>
                <dt>Tipo</dt><dd>${evento.tipo.etiqueta}<c:if test="${not empty evento.categoria}"> · <c:out value="${evento.categoria}" /></c:if></dd>
            </dl>
            <c:if test="${not empty evento.descripcion}">
                <p style="margin-top:14px"><c:out value="${evento.descripcion}" /></p>
            </c:if>
            <div style="margin-top:18px">
                <c:choose>
                    <c:when test="${aLaVenta and (empty u or u.cliente)}">
                        <a class="boton acento" href="${ctx}/cliente/comprar?evento=${evento.id}">Elegir asientos</a>
                        <c:if test="${empty u}">
                            <span class="ayuda" style="display:block;margin-top:6px">Para comprar debe
                                <a href="${ctx}/cliente/comprar?evento=${evento.id}">iniciar sesión</a> o
                                <a href="${ctx}/registro">crear una cuenta</a>.</span>
                        </c:if>
                    </c:when>
                    <c:when test="${aLaVenta}">
                        <span class="ayuda">La compra en línea es para cuentas de cliente.</span>
                    </c:when>
                    <c:otherwise>
                        <div class="alerta aviso"><c:out value="${motivoSinVenta}" /></div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <%-- Precios y disponibilidad por zona (RF-15) --%>
    <section class="tarjeta tabla-contenedor">
        <h2 style="margin-top:0">Zonas y precios</h2>
        <table class="tabla">
            <thead><tr><th>Zona</th><th class="numero">Precio</th><th class="numero">Disponibles</th><th>Disponibilidad</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${precios}">
                <c:set var="disp" value="${null}" />
                <c:forEach var="o" items="${ocupacion}">
                    <c:if test="${o.zona == p.zona}"><c:set var="disp" value="${o}" /></c:if>
                </c:forEach>
                <c:if test="${not empty p.precio}">
                    <tr>
                        <td><strong><c:out value="${p.zona}" /></strong></td>
                        <td class="numero">${p.precioTexto}</td>
                        <td class="numero">${empty disp ? '—' : disp.disponibles}</td>
                        <td style="min-width:160px">
                            <c:if test="${not empty disp and disp.total > 0}">
                                <div class="barra verde"><span style="width:${100 - disp.porcentaje}%"></span></div>
                            </c:if>
                        </td>
                    </tr>
                </c:if>
            </c:forEach>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
