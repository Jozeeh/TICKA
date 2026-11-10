<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Reportes" />
<c:set var="paginaActiva" value="reportes" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <h1 class="h3 mb-1">Reportes</h1>
    <p class="text-body-secondary mb-4">Filtre por fechas, evento, tipo, categoría o zona. El PDF se abre en una pestaña nueva y el CSV se descarga para abrirlo en Excel.</p>
    <c:if test="${tipo == 'general'}">
        <p class="small text-body-secondary">Sin evento elegido se muestra un resumen de todos los eventos; elija un evento para ver toda su información.</p>
    </c:if>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <nav class="nav nav-tabs mb-3">
        <c:forEach var="p" items="general,ventas,asistencia,ocupacion,incidencias">
            <c:url var="enlace" value="/reportes">
                <c:param name="tipo" value="${p}" />
                <c:param name="desde" value="${filtro.desde}" />
                <c:param name="hasta" value="${filtro.hasta}" />
                <c:param name="evento" value="${filtro.idEvento}" />
                <c:param name="tipoEvento" value="${filtro.tipoEvento.bd}" />
                <c:param name="categoria" value="${filtro.idCategoria}" />
                <c:param name="zona" value="${filtro.idZona}" />
            </c:url>
            <a class="nav-link ${tipo == p ? 'active' : ''}" href="${enlace}">
                <c:choose>
                    <c:when test="${p == 'general'}">General</c:when>
                    <c:when test="${p == 'ventas'}">Ventas</c:when>
                    <c:when test="${p == 'asistencia'}">Asistencia</c:when>
                    <c:when test="${p == 'ocupacion'}">Ocupación</c:when>
                    <c:otherwise>Incidencias</c:otherwise>
                </c:choose>
            </a>
        </c:forEach>
    </nav>

    <%-- Filtros (RF-48) --%>
    <form method="get" action="${ctx}/reportes" class="card card-body shadow-sm mb-4 filtros d-flex flex-row flex-wrap gap-3 align-items-end">
        <input type="hidden" name="tipo" value="${tipo}">
        <div class="campo-filtro">
            <label class="form-label" for="desde">Desde</label>
            <input class="form-control" type="date" id="desde" name="desde" value="${filtro.desde}">
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="hasta">Hasta</label>
            <input class="form-control" type="date" id="hasta" name="hasta" value="${filtro.hasta}">
        </div>
        <div class="campo-filtro" style="min-width:240px">
            <label class="form-label" for="evento">Evento</label>
            <select class="form-select" id="evento" name="evento">
                <option value="">Todos</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${filtro.idEvento == ev.id ? 'selected' : ''}>
                        <c:out value="${ev.nombre}" /> (${ev.fechaTexto})
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="tipoEvento">Tipo</label>
            <select class="form-select" id="tipoEvento" name="tipoEvento">
                <option value="">Todos</option>
                <c:forEach var="t" items="${tiposEvento}">
                    <option value="${t.bd}" ${filtro.tipoEvento == t ? 'selected' : ''}>${t.etiqueta}</option>
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
        <c:if test="${tipo != 'incidencias' and tipo != 'general'}">
            <div class="campo-filtro" style="min-width:220px">
                <label class="form-label" for="zona">Zona</label>
                <select class="form-select" id="zona" name="zona">
                    <option value="">Todas</option>
                    <c:forEach var="z" items="${zonas}">
                        <option value="${z.key}" ${filtro.idZona == z.key ? 'selected' : ''}><c:out value="${z.value}" /></option>
                    </c:forEach>
                </select>
            </div>
        </c:if>
        <div class="d-flex flex-wrap gap-2 align-items-center">
            <button type="submit" class="btn btn-primary">Aplicar filtros</button>
            <%-- El PDF se abre en una pestaña nueva; el CSV se descarga. --%>
            <button type="submit" name="formato" value="pdf" formtarget="_blank" class="btn btn-danger">Exportar PDF</button>
            <button type="submit" name="formato" value="csv" class="btn btn-success">Exportar CSV</button>
            <a class="btn btn-secondary" href="${ctx}/reportes?tipo=${tipo}">Limpiar</a>
        </div>
    </form>

    <c:if test="${not empty reporte.indicadores}">
        <section class="row row-cols-2 row-cols-md-3 row-cols-xl-4 g-3 mb-4 indicadores compactos">
            <c:forEach var="k" items="${reporte.indicadores}">
                <div class="col"><div class="card h-100 indicador"><div class="card-body"><div class="valor"><c:out value="${k[1]}" /></div><div class="nombre"><c:out value="${k[0]}" /></div></div></div></div>
            </c:forEach>
        </section>
    </c:if>

    <c:if test="${not empty reporte}">
        <section class="card card-body shadow-sm mb-4 table-responsive">
            <h2 class="h5 mb-3"><c:out value="${reporte.titulo}" /></h2>
            <table class="table table-hover align-middle mb-0">
                <thead>
                <tr>
                    <c:forEach var="col" items="${reporte.columnas}" varStatus="s">
                        <th class="${reporte.numericas.contains(s.index) ? 'text-end' : ''}"><c:out value="${col}" /></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="fila" items="${reporte.filas}">
                    <tr>
                        <c:forEach var="valor" items="${fila}" varStatus="s">
                            <td class="${reporte.numericas.contains(s.index) ? 'text-end' : ''}"><c:out value="${valor}" /></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                <c:if test="${empty reporte.filas}">
                    <tr><td colspan="${reporte.columnas.size()}" class="text-center text-body-secondary py-4">No hay datos con esos filtros.</td></tr>
                </c:if>
                </tbody>
                <c:if test="${not empty reporte.filas and not empty reporte.totales}">
                    <tfoot>
                    <tr>
                        <c:forEach var="valor" items="${reporte.totales}" varStatus="s">
                            <th class="${reporte.numericas.contains(s.index) ? 'text-end' : ''}"><c:out value="${valor}" /></th>
                        </c:forEach>
                    </tr>
                    </tfoot>
                </c:if>
            </table>
        </section>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
