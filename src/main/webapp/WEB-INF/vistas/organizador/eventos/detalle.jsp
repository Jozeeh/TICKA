<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${evento.nombre}" />
<c:set var="paginaActiva" value="eventos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/organizador/eventos">Eventos</a> › <c:out value="${evento.nombre}" /></div>
    <div class="cabecera-pagina">
        <div>
            <h1><c:out value="${evento.nombre}" /> <span class="etiqueta ${evento.estado.clase}">${evento.estado.bd}</span></h1>
            <p class="subtitulo">${evento.tipo.etiqueta} · <c:out value="${evento.recinto}" /> · ${evento.fechaInicioTexto}</p>
        </div>
        <div class="acciones-linea">
            <c:if test="${evento.estado.editable}">
                <a class="boton amarillo" href="${ctx}/organizador/eventos?accion=editar&id=${evento.id}">Editar datos</a>
            </c:if>
            <c:if test="${evento.estado.conMapa}">
                <a class="boton" href="${ctx}/organizador/mapa?evento=${evento.id}">Mapa de asientos</a>
            </c:if>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta">
        <h2 style="margin-top:0">Datos del evento</h2>
        <img class="vista-afiche flotante" alt="Afiche del evento"
             src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
        <dl class="datos">
            <dt>Participantes</dt><dd><c:out value="${evento.participantes}" default="—" /></dd>
            <dt>Categoría</dt><dd><c:out value="${evento.categoria}" default="Sin categoría" /></dd>
            <dt>Apertura de puertas</dt><dd>${evento.aperturaPuertasTexto}</dd>
            <dt>Inicio</dt><dd>${evento.fechaInicioTexto}</dd>
            <dt>Fin</dt><dd>${evento.fechaFinTexto}</dd>
            <dt>Descripción</dt><dd><c:out value="${evento.descripcion}" default="—" /></dd>
        </dl>
    </section>

    <%-- Precios por zona (RF-05) --%>
    <section class="tarjeta">
        <h2 style="margin-top:0">Precios por zona</h2>
        <c:choose>
            <c:when test="${empty precios}">
                <div class="alerta aviso">El recinto no tiene zonas. Agréguelas en
                    <a href="${ctx}/organizador/zonas?recinto=${evento.idRecinto}">Zonas y asientos</a>.</div>
            </c:when>
            <c:otherwise>
                <table class="tabla">
                    <thead><tr><th>Zona</th><th class="numero">Asientos activos</th><th class="numero">Precio</th></tr></thead>
                    <tbody>
                    <c:forEach var="p" items="${precios}">
                        <tr>
                            <td><c:out value="${p.zona}" /></td>
                            <td class="numero">${p.asientosActivos}</td>
                            <td class="numero">${empty p.precio ? 'Sin precio' : p.precioTexto}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <c:if test="${evento.estado.editable}">
                    <p class="ayuda" style="margin-top:10px">Los precios se cambian en
                        <a href="${ctx}/organizador/eventos?accion=editar&id=${evento.id}">Editar datos</a>.</p>
                </c:if>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- Ocupación por zona (solo con mapa generado) --%>
    <c:if test="${not empty ocupacion}">
        <section class="tarjeta tabla-contenedor">
            <h2 style="margin-top:0">Ocupación</h2>
            <table class="tabla">
                <thead>
                <tr><th>Zona</th><th class="numero">Total</th><th class="numero">Vendidos</th>
                    <th class="numero">Reservados</th><th class="numero">Disponibles</th>
                    <th class="numero">Bloqueados</th><th>% vendido</th></tr>
                </thead>
                <tbody>
                <c:forEach var="o" items="${ocupacion}">
                    <tr>
                        <td><c:out value="${o.zona}" /></td>
                        <td class="numero">${o.total}</td>
                        <td class="numero">${o.vendidos}</td>
                        <td class="numero">${o.reservados}</td>
                        <td class="numero">${o.disponibles}</td>
                        <td class="numero">${o.bloqueados}</td>
                        <td>${o.porcentaje}% <div class="barra"><span style="width:${o.porcentaje}%"></span></div></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </c:if>

    <%-- Acciones de estado (RF-02, RF-07, RN-09) --%>
    <section class="tarjeta">
        <h2 style="margin-top:0">Acciones</h2>
        <div class="acciones-linea">
            <c:if test="${evento.estado == 'BORRADOR'}">
                <form method="post" action="${ctx}/organizador/eventos" class="linea"
                      onsubmit="return confirm('¿Publicar el evento? Se generará el mapa de asientos y quedará a la venta.');">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="publicar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="boton acento">Publicar evento</button>
                </form>
            </c:if>
            <c:if test="${not empty siguienteEstado}">
                <form method="post" action="${ctx}/organizador/eventos" class="linea"
                      onsubmit="return confirm('¿Cambiar el estado a ${siguienteEstado.bd}?');">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="avanzar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="boton">Marcar como ${siguienteEstado.bd}</button>
                </form>
            </c:if>
            <c:if test="${evento.estado.cancelable}">
                <form method="post" action="${ctx}/organizador/eventos" class="linea"
                      onsubmit="return confirm('¿Cancelar el evento? Se anularán todos sus boletos (reembolso simulado). Esta acción no se puede deshacer.');">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="cancelar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="boton rojo">Cancelar evento</button>
                </form>
            </c:if>
            <c:if test="${evento.estado == 'FINALIZADO' or evento.estado == 'CANCELADO'}">
                <span class="ayuda">El evento está ${evento.estado.bd.toLowerCase()}: solo se puede consultar.</span>
            </c:if>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
