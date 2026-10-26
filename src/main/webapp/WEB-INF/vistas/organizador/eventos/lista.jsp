<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Eventos" />
<c:set var="paginaActiva" value="eventos" />
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
            <h1>Eventos</h1>
            <p class="subtitulo">Conciertos, partidos, ópera y teatro. ${total} eventos encontrados.</p>
        </div>
        <div class="acciones-linea">
            <a class="boton borde" href="${ctx}/organizador/calendario">Ver calendario</a>
            <a class="boton verde" href="${ctx}/organizador/eventos?accion=nuevo">Nuevo evento</a>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Filtros (RF-03): se envían por GET para que la URL se pueda compartir --%>
    <form method="get" action="${ctx}/organizador/eventos" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:220px">
            <label for="texto">Buscar</label>
            <input type="text" id="texto" name="texto" placeholder="Nombre o participantes"
                   value="<c:out value='${filtro.texto}' />">
        </div>
        <div class="grupo-campo">
            <label for="tipo">Tipo</label>
            <select id="tipo" name="tipo">
                <option value="">Todos</option>
                <c:forEach var="t" items="${tipos}">
                    <option value="${t.bd}" ${filtro.tipo == t ? 'selected' : ''}>${t.etiqueta}</option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="estado">Estado</label>
            <select id="estado" name="estado">
                <option value="">Todos</option>
                <c:forEach var="es" items="${estados}">
                    <option value="${es}" ${filtro.estado == es ? 'selected' : ''}>${es.bd}</option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="recinto">Recinto</label>
            <select id="recinto" name="recinto">
                <option value="">Todos</option>
                <c:forEach var="r" items="${recintos}">
                    <option value="${r.id}" ${filtro.idRecinto == r.id ? 'selected' : ''}><c:out value="${r.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="categoria">Categoría</label>
            <select id="categoria" name="categoria">
                <option value="">Todas</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}" ${filtro.idCategoria == cat.id ? 'selected' : ''}><c:out value="${cat.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="desde">Desde</label>
            <input type="date" id="desde" name="desde" value="${filtro.desde}">
        </div>
        <div class="grupo-campo">
            <label for="hasta">Hasta</label>
            <input type="date" id="hasta" name="hasta" value="${filtro.hasta}">
        </div>
        <label class="casilla" style="align-self:center">
            <input type="checkbox" name="mios" value="si" ${not empty filtro.idCreador ? 'checked' : ''}> Solo mis eventos
        </label>
        <button type="submit" class="boton">Filtrar</button>
        <a class="boton gris" href="${ctx}/organizador/eventos">Limpiar</a>
    </form>

    <section class="tarjeta tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr>
                <th>Fecha</th><th>Evento</th><th>Tipo</th><th>Recinto</th>
                <th>Estado</th><th>Vendidos</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ev" items="${eventos}">
                <tr>
                    <td><strong>${ev.fechaTexto}</strong><br><span class="ayuda">${ev.horarioTexto}</span></td>
                    <td><strong><c:out value="${ev.nombre}" /></strong><br>
                        <span class="ayuda"><c:out value="${ev.participantes}" /></span></td>
                    <td>${ev.tipo.etiqueta}</td>
                    <td><c:out value="${ev.recinto}" /></td>
                    <td><span class="etiqueta ${ev.estado.clase}">${ev.estado.bd}</span></td>
                    <td>
                        <c:choose>
                            <c:when test="${ev.totalAsientos > 0}">
                                ${ev.vendidos} / ${ev.totalAsientos}
                                <div class="barra"><span style="width:${ev.porcentajeVendido}%"></span></div>
                            </c:when>
                            <c:otherwise><span class="ayuda">Sin publicar</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <div class="acciones-linea">
                            <a class="boton chico" href="${ctx}/organizador/eventos?accion=ver&id=${ev.id}">Ver</a>
                            <c:if test="${ev.estado.conMapa}">
                                <a class="boton borde chico" href="${ctx}/organizador/mapa?evento=${ev.id}">Mapa</a>
                            </c:if>
                            <a class="boton gris chico" href="${ctx}/organizador/eventos?accion=duplicar&id=${ev.id}">Duplicar</a>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty eventos}">
                <tr><td colspan="7" class="vacio">No hay eventos con esos criterios.</td></tr>
            </c:if>
            </tbody>
        </table>

        <c:if test="${paginas > 1}">
            <div class="paginacion">
                <c:forEach var="p" begin="1" end="${paginas}">
                    <c:url var="enlace" value="/organizador/eventos">
                        <c:param name="texto" value="${filtro.texto}" />
                        <c:param name="tipo" value="${filtro.tipo.bd}" />
                        <c:param name="estado" value="${filtro.estado}" />
                        <c:param name="recinto" value="${filtro.idRecinto}" />
                        <c:param name="categoria" value="${filtro.idCategoria}" />
                        <c:param name="mios" value="${not empty filtro.idCreador ? 'si' : ''}" />
                        <c:param name="desde" value="${filtro.desde}" />
                        <c:param name="hasta" value="${filtro.hasta}" />
                        <c:param name="pagina" value="${p}" />
                    </c:url>
                    <a class="boton chico ${p == filtro.pagina ? '' : 'borde'}" href="${enlace}">${p}</a>
                </c:forEach>
                <span>Página ${filtro.pagina} de ${paginas}</span>
            </div>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
