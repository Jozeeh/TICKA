<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Calendario de eventos" />
<c:set var="paginaActiva" value="calendario" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="cabecera-pagina">
        <div>
            <h1>Calendario de eventos</h1>
            <p class="subtitulo">${totalMes} eventos en ${tituloMes}. Haga clic en un evento para ver su detalle.</p>
        </div>
        <div class="acciones-linea">
            <a class="boton borde" href="${ctx}/organizador/eventos">Ver listado</a>
            <a class="boton verde" href="${ctx}/organizador/eventos?accion=nuevo">Nuevo evento</a>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Filtros (RF-50, RF-52) --%>
    <form method="get" action="${ctx}/organizador/calendario" class="tarjeta filtros">
        <input type="hidden" name="mes" value="${mes}">
        <div class="grupo-campo">
            <label for="recinto">Recinto</label>
            <select id="recinto" name="recinto" onchange="this.form.submit()">
                <option value="">Todos</option>
                <c:forEach var="r" items="${recintos}">
                    <option value="${r.id}" ${idRecinto == r.id ? 'selected' : ''}><c:out value="${r.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <label class="casilla" style="align-self:center">
            <input type="checkbox" name="mios" value="si" ${mios ? 'checked' : ''} onchange="this.form.submit()"> Solo mis eventos
        </label>
    </form>

    <section class="tarjeta calendario">
        <div class="calendario-cabecera">
            <c:url var="anterior" value="/organizador/calendario">
                <c:param name="mes" value="${mesAnterior}" />
                <c:param name="recinto" value="${idRecinto}" />
                <c:param name="mios" value="${mios ? 'si' : ''}" />
            </c:url>
            <c:url var="siguiente" value="/organizador/calendario">
                <c:param name="mes" value="${mesSiguiente}" />
                <c:param name="recinto" value="${idRecinto}" />
                <c:param name="mios" value="${mios ? 'si' : ''}" />
            </c:url>
            <c:url var="actual" value="/organizador/calendario">
                <c:param name="mes" value="${mesActual}" />
                <c:param name="recinto" value="${idRecinto}" />
                <c:param name="mios" value="${mios ? 'si' : ''}" />
            </c:url>
            <a class="boton borde chico" href="${anterior}">‹ Anterior</a>
            <h2>${tituloMes}</h2>
            <div class="acciones-linea">
                <a class="boton gris chico" href="${actual}">Hoy</a>
                <a class="boton borde chico" href="${siguiente}">Siguiente ›</a>
            </div>
        </div>

        <div class="calendario-rejilla">
            <div class="dia-semana">Lun</div><div class="dia-semana">Mar</div><div class="dia-semana">Mié</div>
            <div class="dia-semana">Jue</div><div class="dia-semana">Vie</div><div class="dia-semana">Sáb</div>
            <div class="dia-semana">Dom</div>
            <c:forEach var="semana" items="${semanas}">
                <c:forEach var="dia" items="${semana}">
                    <div class="celda-dia ${dia.delMes ? '' : 'fuera'} ${dia.hoy ? 'hoy' : ''}">
                        <span class="numero-dia">${dia.dia}</span>
                        <c:forEach var="ev" items="${dia.eventos}">
                            <a class="evento-cal ${ev.estado.clase}" href="${ctx}/organizador/eventos?accion=ver&id=${ev.id}"
                               title="<c:out value='${ev.nombre}' /> · <c:out value='${ev.recinto}' /> · ${ev.estado.bd}">
                                <strong>${ev.horarioTexto}</strong>
                                <span><c:out value="${ev.nombre}" /></span>
                            </a>
                        </c:forEach>
                    </div>
                </c:forEach>
            </c:forEach>
        </div>

        <div class="leyenda-cal">
            <span class="evento-cal Borrador">Borrador</span>
            <span class="evento-cal Publicado">Publicado</span>
            <span class="evento-cal Agotado">Agotado</span>
            <span class="evento-cal Encurso">En curso</span>
            <span class="evento-cal Finalizado">Finalizado</span>
            <span class="evento-cal Cancelado">Cancelado</span>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
