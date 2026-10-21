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
<main class="contenedor">
    <h1>Reportes</h1>
    <p class="subtitulo">Filtre por fechas, evento, tipo, categoría o zona y exporte a PDF o a CSV para abrirlo en Excel.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <nav class="pestanas">
        <c:forEach var="p" items="ventas,asistencia,ocupacion,incidencias">
            <c:url var="enlace" value="/admin/reportes">
                <c:param name="tipo" value="${p}" />
                <c:param name="desde" value="${filtro.desde}" />
                <c:param name="hasta" value="${filtro.hasta}" />
                <c:param name="evento" value="${filtro.idEvento}" />
                <c:param name="tipoEvento" value="${filtro.tipoEvento.bd}" />
                <c:param name="categoria" value="${filtro.idCategoria}" />
                <c:param name="zona" value="${filtro.idZona}" />
            </c:url>
            <a class="${tipo == p ? 'activa' : ''}" href="${enlace}">
                <c:choose>
                    <c:when test="${p == 'ventas'}">Ventas</c:when>
                    <c:when test="${p == 'asistencia'}">Asistencia</c:when>
                    <c:when test="${p == 'ocupacion'}">Ocupación</c:when>
                    <c:otherwise>Incidencias</c:otherwise>
                </c:choose>
            </a>
        </c:forEach>
    </nav>

    <%-- Filtros (RF-48) --%>
    <form method="get" action="${ctx}/admin/reportes" class="tarjeta filtros">
        <input type="hidden" name="tipo" value="${tipo}">
        <div class="grupo-campo">
            <label for="desde">Desde</label>
            <input type="date" id="desde" name="desde" value="${filtro.desde}">
        </div>
        <div class="grupo-campo">
            <label for="hasta">Hasta</label>
            <input type="date" id="hasta" name="hasta" value="${filtro.hasta}">
        </div>
        <div class="grupo-campo" style="min-width:240px">
            <label for="evento">Evento</label>
            <select id="evento" name="evento">
                <option value="">Todos</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${filtro.idEvento == ev.id ? 'selected' : ''}>
                        <c:out value="${ev.nombre}" /> (${ev.fechaTexto})
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="tipoEvento">Tipo</label>
            <select id="tipoEvento" name="tipoEvento">
                <option value="">Todos</option>
                <c:forEach var="t" items="${tiposEvento}">
                    <option value="${t.bd}" ${filtro.tipoEvento == t ? 'selected' : ''}>${t.etiqueta}</option>
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
        <c:if test="${tipo != 'incidencias'}">
            <div class="grupo-campo" style="min-width:220px">
                <label for="zona">Zona</label>
                <select id="zona" name="zona">
                    <option value="">Todas</option>
                    <c:forEach var="z" items="${zonas}">
                        <option value="${z.key}" ${filtro.idZona == z.key ? 'selected' : ''}><c:out value="${z.value}" /></option>
                    </c:forEach>
                </select>
            </div>
        </c:if>
        <div class="acciones-linea">
            <button type="submit" class="boton">Aplicar filtros</button>
            <button type="submit" name="formato" value="pdf" class="boton rojo">Exportar PDF</button>
            <button type="submit" name="formato" value="csv" class="boton verde">Exportar CSV</button>
            <a class="boton gris" href="${ctx}/admin/reportes?tipo=${tipo}">Limpiar</a>
        </div>
    </form>

    <c:if test="${not empty reporte}">
        <section class="tarjeta tabla-contenedor">
            <h2 style="margin-top:0"><c:out value="${reporte.titulo}" /></h2>
            <table class="tabla">
                <thead>
                <tr>
                    <c:forEach var="col" items="${reporte.columnas}" varStatus="s">
                        <th class="${reporte.numericas.contains(s.index) ? 'numero' : ''}"><c:out value="${col}" /></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="fila" items="${reporte.filas}">
                    <tr>
                        <c:forEach var="valor" items="${fila}" varStatus="s">
                            <td class="${reporte.numericas.contains(s.index) ? 'numero' : ''}"><c:out value="${valor}" /></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                <c:if test="${empty reporte.filas}">
                    <tr><td colspan="${reporte.columnas.size()}" class="vacio">No hay datos con esos filtros.</td></tr>
                </c:if>
                </tbody>
                <c:if test="${not empty reporte.filas and not empty reporte.totales}">
                    <tfoot>
                    <tr>
                        <c:forEach var="valor" items="${reporte.totales}" varStatus="s">
                            <th class="${reporte.numericas.contains(s.index) ? 'numero' : ''}"><c:out value="${valor}" /></th>
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
