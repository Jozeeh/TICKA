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
    <p class="subtitulo">Filtre por rango de fechas del evento o por un evento específico y exporte a CSV para abrirlo en Excel.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <nav class="pestanas">
        <c:forEach var="p" items="ventas,asistencia,ocupacion,incidencias">
            <c:url var="enlace" value="/admin/reportes">
                <c:param name="tipo" value="${p}" />
                <c:param name="desde" value="${desde}" />
                <c:param name="hasta" value="${hasta}" />
                <c:param name="evento" value="${idEvento}" />
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

    <form method="get" action="${ctx}/admin/reportes" class="tarjeta filtros">
        <input type="hidden" name="tipo" value="${tipo}">
        <div class="grupo-campo">
            <label for="desde">Desde</label>
            <input type="date" id="desde" name="desde" value="${desde}">
        </div>
        <div class="grupo-campo">
            <label for="hasta">Hasta</label>
            <input type="date" id="hasta" name="hasta" value="${hasta}">
        </div>
        <div class="grupo-campo" style="min-width:260px">
            <label for="evento">Evento</label>
            <select id="evento" name="evento">
                <option value="">Todos</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${idEvento == ev.id ? 'selected' : ''}>
                        <c:out value="${ev.nombre}" /> (${ev.fechaTexto})
                    </option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="boton">Aplicar filtros</button>
        <button type="submit" name="formato" value="csv" class="boton verde">Exportar CSV</button>
        <a class="boton gris" href="${ctx}/admin/reportes?tipo=${tipo}">Limpiar</a>
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
