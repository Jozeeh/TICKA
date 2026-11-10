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
<main class="container-xl py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1">Eventos</h1>
            <p class="text-body-secondary mb-0">Conciertos, partidos, ópera y teatro. ${total} eventos encontrados.</p>
        </div>
        <div class="d-flex flex-wrap gap-2 align-items-center">
            <a class="btn btn-outline-primary" href="${ctx}/organizador/calendario">Ver calendario</a>
            <a class="btn btn-success" href="${ctx}/organizador/eventos?accion=nuevo">Nuevo evento</a>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Filtros (RF-03): se envían por GET para que la URL se pueda compartir --%>
    <form method="get" action="${ctx}/organizador/eventos" class="card card-body shadow-sm mb-4 filtros d-flex flex-row flex-wrap gap-3 align-items-end">
        <div class="campo-filtro" style="min-width:220px">
            <label class="form-label" for="texto">Buscar</label>
            <input class="form-control" type="text" id="texto" name="texto" placeholder="Nombre o participantes"
                   value="<c:out value='${filtro.texto}' />">
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="tipo">Tipo</label>
            <select class="form-select" id="tipo" name="tipo">
                <option value="">Todos</option>
                <c:forEach var="t" items="${tipos}">
                    <option value="${t.bd}" ${filtro.tipo == t ? 'selected' : ''}>${t.etiqueta}</option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="estado">Estado</label>
            <select class="form-select" id="estado" name="estado">
                <option value="">Todos</option>
                <c:forEach var="es" items="${estados}">
                    <option value="${es}" ${filtro.estado == es ? 'selected' : ''}>${es.bd}</option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="recinto">Recinto</label>
            <select class="form-select" id="recinto" name="recinto">
                <option value="">Todos</option>
                <c:forEach var="r" items="${recintos}">
                    <option value="${r.id}" ${filtro.idRecinto == r.id ? 'selected' : ''}><c:out value="${r.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="categoria">Categoría</label>
            <select class="form-select" id="categoria" name="categoria">
                <option value="">Todas</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}" ${filtro.idCategoria == cat.id ? 'selected' : ''}><c:out value="${cat.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="desde">Desde</label>
            <input class="form-control" type="date" id="desde" name="desde" value="${filtro.desde}">
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="hasta">Hasta</label>
            <input class="form-control" type="date" id="hasta" name="hasta" value="${filtro.hasta}">
        </div>
        <label class="form-check align-self-center mb-2">
            <input class="form-check-input" type="checkbox" name="mios" value="si" ${not empty filtro.idCreador ? 'checked' : ''}> Solo mis eventos
        </label>
        <button type="submit" class="btn btn-primary">Filtrar</button>
        <a class="btn btn-secondary" href="${ctx}/organizador/eventos">Limpiar</a>
    </form>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr>
                <th>Fecha</th><th>Evento</th><th>Tipo</th><th>Recinto</th>
                <th>Estado</th><th>Vendidos</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ev" items="${eventos}">
                <tr>
                    <td><strong>${ev.fechaTexto}</strong><br><span class="small text-body-secondary">${ev.horarioTexto}</span></td>
                    <td><strong><c:out value="${ev.nombre}" /></strong><br>
                        <span class="small text-body-secondary"><c:out value="${ev.participantes}" /></span></td>
                    <td>${ev.tipo.etiqueta}</td>
                    <td><c:out value="${ev.recinto}" /></td>
                    <td><span class="badge rounded-pill etiqueta ${ev.estado.clase}">${ev.estado.bd}</span></td>
                    <td>
                        <c:choose>
                            <c:when test="${ev.totalAsientos > 0}">
                                ${ev.vendidos} / ${ev.totalAsientos}
                                <div class="progress mt-1" role="progressbar" style="height:8px"><div class="progress-bar bg-success" style="width:${ev.porcentajeVendido}%"></div></div>
                            </c:when>
                            <c:otherwise><span class="small text-body-secondary">Sin publicar</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <a class="btn btn-primary btn-sm" href="${ctx}/organizador/eventos?accion=ver&id=${ev.id}">Ver</a>
                            <c:if test="${ev.estado.conMapa}">
                                <a class="btn btn-outline-primary btn-sm" href="${ctx}/organizador/mapa?evento=${ev.id}">Mapa</a>
                            </c:if>
                            <a class="btn btn-secondary btn-sm" href="${ctx}/organizador/eventos?accion=duplicar&id=${ev.id}">Duplicar</a>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty eventos}">
                <tr><td colspan="7" class="text-center text-body-secondary py-4">No hay eventos con esos criterios.</td></tr>
            </c:if>
            </tbody>
        </table>

        <c:if test="${paginas > 1}">
            <nav class="d-flex flex-wrap align-items-center gap-3 mt-3" aria-label="Paginación">
                <ul class="pagination pagination-sm mb-0">
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
                    <li class="page-item ${p == filtro.pagina ? 'active' : ''}"><a class="page-link" href="${enlace}">${p}</a></li>
                </c:forEach>
                </ul>
                <span class="small text-body-secondary">Página ${filtro.pagina} de ${paginas}</span>
            </nav>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
