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
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/#cartelera">Cartelera</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${evento.nombre}" /></li></ol></nav>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="row g-4 mb-4 align-items-start">
        <div class="col-md-5 col-lg-4">
            <img class="afiche-grande img-fluid rounded-3 border" alt="Afiche de ${evento.nombre}"
                 src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
        </div>
        <div class="col-md-7 col-lg-8">
            <span class="badge rounded-pill etiqueta ${evento.estado.clase}">${evento.estado.bd}</span>
            <h1 class="h3 mt-2 mb-1"><c:out value="${evento.nombre}" /></h1>
            <p class="mb-3"><c:out value="${evento.participantes}" /></p>
            <dl class="row mb-0">
                <dt class="col-sm-4 col-lg-3 text-body-secondary">Fecha</dt><dd class="col-sm-8 col-lg-9 mb-1">${evento.fechaLarga}</dd>
                <dt class="col-sm-4 col-lg-3 text-body-secondary">Horario</dt><dd class="col-sm-8 col-lg-9 mb-1">${evento.horarioTexto} · puertas desde ${evento.aperturaPuertasTexto}</dd>
                <dt class="col-sm-4 col-lg-3 text-body-secondary">Lugar</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${evento.recinto}" /></dd>
                <dt class="col-sm-4 col-lg-3 text-body-secondary">Tipo</dt><dd class="col-sm-8 col-lg-9 mb-1">${evento.tipo.etiqueta}<c:if test="${not empty evento.categoria}"> · <c:out value="${evento.categoria}" /></c:if></dd>
            </dl>
            <c:if test="${not empty evento.descripcion}">
                <p class="mt-3"><c:out value="${evento.descripcion}" /></p>
            </c:if>
            <div class="mt-4">
                <c:choose>
                    <c:when test="${aLaVenta and (empty u or u.cliente)}">
                        <a class="btn btn-acento" href="${ctx}/cliente/comprar?evento=${evento.id}">Elegir asientos</a>
                        <c:if test="${empty u}">
                            <span class="small text-body-secondary d-block mt-2">Para comprar debe
                                <a href="${ctx}/cliente/comprar?evento=${evento.id}">iniciar sesión</a> o
                                <a href="${ctx}/registro">crear una cuenta</a>.</span>
                        </c:if>
                    </c:when>
                    <c:when test="${aLaVenta}">
                        <span class="small text-body-secondary">La compra en línea es para cuentas de cliente.</span>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-warning"><c:out value="${motivoSinVenta}" /></div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>

    <%-- Precios y disponibilidad por zona (RF-15) --%>
    <section class="card card-body shadow-sm mb-4 table-responsive">
        <h2 class="h5 mb-3">Zonas y precios</h2>
        <table class="table table-hover align-middle mb-0">
            <thead><tr><th>Zona</th><th class="text-end">Precio</th><th class="text-end">Disponibles</th><th>Disponibilidad</th></tr></thead>
            <tbody>
            <c:forEach var="p" items="${precios}">
                <c:set var="disp" value="${null}" />
                <c:forEach var="o" items="${ocupacion}">
                    <c:if test="${o.zona == p.zona}"><c:set var="disp" value="${o}" /></c:if>
                </c:forEach>
                <c:if test="${not empty p.precio}">
                    <tr>
                        <td><strong><c:out value="${p.zona}" /></strong></td>
                        <td class="text-end">${p.precioTexto}</td>
                        <td class="text-end">${empty disp ? '—' : disp.disponibles}</td>
                        <td style="min-width:160px">
                            <c:if test="${not empty disp and disp.total > 0}">
                                <div class="progress mt-1" role="progressbar" style="height:8px"><div class="progress-bar bg-success" style="width:${100 - disp.porcentaje}%"></div></div>
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
