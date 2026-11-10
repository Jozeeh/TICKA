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
<main class="container-xl py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1">Dashboard</h1>
            <p class="text-body-secondary mb-0">Resumen general de ventas, ocupación y asistencia.</p>
        </div>
        <a class="btn btn-primary" href="${ctx}/reportes">Ver reportes</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="row row-cols-2 row-cols-md-3 row-cols-xl-4 g-3 mb-4 indicadores">
        <div class="col"><div class="card h-100 indicador"><div class="card-body"><div class="valor">${indicadores.eventosActivos}</div><div class="nombre">Eventos activos</div></div></div></div>
        <div class="col"><div class="card h-100 indicador acento"><div class="card-body"><div class="valor">${indicadores.boletosVendidos}</div><div class="nombre">Boletos vendidos</div></div></div></div>
        <div class="col"><div class="card h-100 indicador verde"><div class="card-body"><div class="valor">${indicadores.ingresosTexto}</div><div class="nombre">Ingresos registrados</div></div></div></div>
        <div class="col"><div class="card h-100 indicador"><div class="card-body"><div class="valor">${indicadores.ocupacion}%</div><div class="nombre">Ocupación de eventos activos</div></div></div></div>
        <div class="col"><div class="card h-100 indicador verde"><div class="card-body"><div class="valor">${indicadores.asistencia}%</div><div class="nombre">Asistencia en eventos finalizados</div></div></div></div>
        <div class="col"><div class="card h-100 indicador amarillo"><div class="card-body"><div class="valor">${indicadores.incidenciasAbiertas}</div><div class="nombre">Incidencias abiertas</div></div></div></div>
        <div class="col"><div class="card h-100 indicador"><div class="card-body"><div class="valor">${indicadores.clientesActivos}</div><div class="nombre">Clientes activos</div></div></div></div>
    </section>

    <%-- Gráficos (RF-53) con Chart.js (copia local). Las series llegan en JSON desde el servlet. --%>
    <c:if test="${not empty graficoMeses}">
        <section class="row row-cols-1 row-cols-lg-2 g-4 mb-4">
            <div class="col"><div class="card card-body shadow-sm h-100"><h2 class="h6 mb-3">Ingresos por mes</h2><div class="lienzo"><canvas id="gMeses"></canvas></div></div></div>
            <div class="col"><div class="card card-body shadow-sm h-100"><h2 class="h6 mb-3">Ingresos por canal</h2><div class="lienzo"><canvas id="gCanal"></canvas></div></div></div>
            <div class="col"><div class="card card-body shadow-sm h-100"><h2 class="h6 mb-3">Eventos con más ingresos</h2><div class="lienzo"><canvas id="gTop"></canvas></div></div></div>
            <div class="col"><div class="card card-body shadow-sm h-100"><h2 class="h6 mb-3">Ocupación de los próximos eventos</h2><div class="lienzo"><canvas id="gOcupacion"></canvas></div></div></div>
        </section>
    </c:if>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <h2 class="h5 mb-3">Próximos eventos</h2>
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr><th>Fecha</th><th>Evento</th><th>Recinto</th><th>Estado</th><th>Vendidos</th><th></th></tr>
            </thead>
            <tbody>
            <c:forEach var="ev" items="${proximos}">
                <tr>
                    <td>${ev.fechaInicioTexto}</td>
                    <td><strong><c:out value="${ev.nombre}" /></strong> <span class="small text-body-secondary">${ev.tipo.etiqueta}</span></td>
                    <td><c:out value="${ev.recinto}" /></td>
                    <td><span class="badge rounded-pill etiqueta ${ev.estado.clase}">${ev.estado.bd}</span></td>
                    <td style="min-width:180px">${ev.vendidos} / ${ev.totalAsientos} (${ev.porcentajeVendido}%)
                        <div class="progress mt-1" role="progressbar" style="height:8px"><div class="progress-bar bg-success" style="width:${ev.porcentajeVendido}%"></div></div></td>
                    <td><a class="btn btn-sm btn-outline-primary" href="${ctx}/organizador/eventos?accion=ver&id=${ev.id}">Ver</a></td>
                </tr>
            </c:forEach>
            <c:if test="${empty proximos}">
                <tr><td colspan="6" class="text-center text-body-secondary py-4">No hay eventos publicados o en curso.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>

    <c:if test="${not empty ocupacion}">
        <section class="card card-body shadow-sm mb-4 table-responsive">
            <h2 class="h5 mb-3"><c:out value="${ocupacion.titulo}" /></h2>
            <table class="table table-hover align-middle mb-0">
                <thead>
                <tr>
                    <c:forEach var="col" items="${ocupacion.columnas}" varStatus="s">
                        <th class="${ocupacion.numericas.contains(s.index) ? 'text-end' : ''}"><c:out value="${col}" /></th>
                    </c:forEach>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="fila" items="${ocupacion.filas}">
                    <tr>
                        <c:forEach var="valor" items="${fila}" varStatus="s">
                            <td class="${ocupacion.numericas.contains(s.index) ? 'text-end' : ''}"><c:out value="${valor}" /></td>
                        </c:forEach>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
<c:if test="${not empty graficoMeses}">
<script src="${ctx}/resources/js/chart.umd.min.js"></script>
<script>
    (function () {
        if (typeof Chart === 'undefined') {
            return;
        }
        var AZUL = '#1f3a5f', NARANJA = '#e4572e', VERDE = '#2e8b57', AZUL_CLARO = '#2f6fb3';
        var dinero = function (v) { return '$' + Number(v).toLocaleString('en-US', {minimumFractionDigits: 0}); };
        Chart.defaults.font.family = '"Segoe UI", Arial, sans-serif';
        Chart.defaults.plugins.legend.display = false;
        Chart.defaults.maintainAspectRatio = false;
        Chart.defaults.animation = false;

        var meses = ${graficoMeses};
        new Chart(document.getElementById('gMeses'), {
            type: 'bar',
            data: {labels: meses.etiquetas, datasets: [{data: meses.valores, backgroundColor: AZUL, borderRadius: 4}]},
            options: {scales: {y: {beginAtZero: true, ticks: {callback: dinero}}},
                plugins: {tooltip: {callbacks: {label: function (c) { return dinero(c.raw); }}}}}
        });

        var canal = ${graficoCanal};
        new Chart(document.getElementById('gCanal'), {
            type: 'doughnut',
            data: {labels: canal.etiquetas, datasets: [{data: canal.valores, backgroundColor: [AZUL, NARANJA, VERDE]}]},
            options: {plugins: {legend: {display: true, position: 'bottom'},
                tooltip: {callbacks: {label: function (c) { return c.label + ': ' + dinero(c.raw); }}}}}
        });

        var top = ${graficoTop};
        new Chart(document.getElementById('gTop'), {
            type: 'bar',
            data: {labels: top.etiquetas, datasets: [{data: top.valores, backgroundColor: VERDE, borderRadius: 4}]},
            options: {indexAxis: 'y', scales: {x: {beginAtZero: true, ticks: {callback: dinero}}},
                plugins: {tooltip: {callbacks: {label: function (c) { return dinero(c.raw); }}}}}
        });

        var ocupacion = ${graficoOcupacion};
        new Chart(document.getElementById('gOcupacion'), {
            type: 'bar',
            data: {labels: ocupacion.etiquetas, datasets: [{data: ocupacion.valores, backgroundColor: AZUL_CLARO, borderRadius: 4}]},
            options: {indexAxis: 'y', scales: {x: {beginAtZero: true, max: 100, ticks: {callback: function (v) { return v + '%'; }}}},
                plugins: {tooltip: {callbacks: {label: function (c) { return c.raw + '% vendido'; }}}}}
        });
    })();
</script>
</c:if>
</body>
</html>
