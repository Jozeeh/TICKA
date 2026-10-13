<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Dashboard" />
<c:set var="paginaActiva" value="dashboard" />
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
            <h1>Dashboard</h1>
            <p class="subtitulo">Resumen general de ventas, ocupación y asistencia.</p>
        </div>
        <a class="boton" href="${ctx}/admin/reportes">Ver reportes</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="indicadores">
        <div class="indicador"><div class="valor">${indicadores.eventosActivos}</div><div class="nombre">Eventos activos</div></div>
        <div class="indicador acento"><div class="valor">${indicadores.boletosVendidos}</div><div class="nombre">Boletos vendidos</div></div>
        <div class="indicador verde"><div class="valor">${indicadores.ingresosTexto}</div><div class="nombre">Ingresos registrados</div></div>
        <div class="indicador"><div class="valor">${indicadores.ocupacion}%</div><div class="nombre">Ocupación de eventos activos</div></div>
        <div class="indicador verde"><div class="valor">${indicadores.asistencia}%</div><div class="nombre">Asistencia en eventos finalizados</div></div>
        <div class="indicador amarillo"><div class="valor">${indicadores.incidenciasAbiertas}</div><div class="nombre">Incidencias abiertas</div></div>
        <div class="indicador"><div class="valor">${indicadores.clientesActivos}</div><div class="nombre">Clientes activos</div></div>
    </section>

    <section class="tarjeta tabla-contenedor">
        <h2 style="margin-top:0">Próximos eventos</h2>
        <table class="tabla">
            <thead>
            <tr><th>Fecha</th><th>Evento</th><th>Recinto</th><th>Estado</th><th>Vendidos</th><th></th></tr>
            </thead>
            <tbody>
            <c:forEach var="ev" items="${proximos}">
                <tr>
                    <td>${ev.fechaInicioTexto}</td>
                    <td><strong><c:out value="${ev.nombre}" /></strong> <span class="ayuda">${ev.tipo.etiqueta}</span></td>
                    <td><c:out value="${ev.recinto}" /></td>
                    <td><span class="etiqueta ${ev.estado.clase}">${ev.estado.bd}</span></td>
                    <td style="min-width:180px">${ev.vendidos} / ${ev.totalAsientos} (${ev.porcentajeVendido}%)
                        <div class="barra"><span style="width:${ev.porcentajeVendido}%"></span></div></td>
                    <td><a class="boton chico borde" href="${ctx}/organizador/eventos?accion=ver&id=${ev.id}">Ver</a></td>
                </tr>
            </c:forEach>
            <c:if test="${empty proximos}">
                <tr><td colspan="6" class="vacio">No hay eventos publicados o en curso.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <c:if test="${not empty ocupacion}">
        <section class="tarjeta tabla-contenedor">
            <h2 style="margin-top:0"><c:out value="${ocupacion.titulo}" /></h2>
            <table class="tabla">
                <thead>
                <tr>
                    <c:forEach var="col" items="${ocupacion.columnas}" varStatus="s">
                        <th class="${ocupacion.numericas.contains(s.index) ? 'numero' : ''}"><c:out value="${col}" /></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="fila" items="${ocupacion.filas}">
                    <tr>
                        <c:forEach var="valor" items="${fila}" varStatus="s">
                            <td class="${ocupacion.numericas.contains(s.index) ? 'numero' : ''}"><c:out value="${valor}" /></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
