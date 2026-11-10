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
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/organizador/eventos">Eventos</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${evento.nombre}" /></li></ol></nav>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1"><c:out value="${evento.nombre}" /> <span class="badge rounded-pill etiqueta ${evento.estado.clase}">${evento.estado.bd}</span></h1>
            <p class="text-body-secondary mb-0">${evento.tipo.etiqueta} · <c:out value="${evento.recinto}" /> · ${evento.fechaInicioTexto}</p>
        </div>
        <div class="d-flex flex-wrap gap-2 align-items-center">
            <c:if test="${evento.estado.editable}">
                <a class="btn btn-warning" href="${ctx}/organizador/eventos?accion=editar&id=${evento.id}">Editar datos</a>
            </c:if>
            <c:if test="${evento.estado.conMapa}">
                <a class="btn btn-primary" href="${ctx}/organizador/mapa?evento=${evento.id}">Mapa de asientos</a>
            </c:if>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">Datos del evento</h2>
        <img class="vista-afiche flotante" alt="Afiche del evento"
             src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
        <dl class="row mb-0">
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Participantes</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${evento.participantes}" default="—" /></dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Categoría</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${evento.categoria}" default="Sin categoría" /></dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Apertura de puertas</dt><dd class="col-sm-8 col-lg-9 mb-1">${evento.aperturaPuertasTexto}</dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Inicio</dt><dd class="col-sm-8 col-lg-9 mb-1">${evento.fechaInicioTexto}</dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Fin</dt><dd class="col-sm-8 col-lg-9 mb-1">${evento.fechaFinTexto}</dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Descripción</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${evento.descripcion}" default="—" /></dd>
        </dl>
    </section>

    <%-- Precios por zona (RF-05) --%>
    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">Precios por zona</h2>
        <c:choose>
            <c:when test="${empty precios}">
                <div class="alert alert-warning">El recinto no tiene zonas. Agréguelas en
                    <a href="${ctx}/organizador/zonas?recinto=${evento.idRecinto}">Zonas y asientos</a>.</div>
            </c:when>
            <c:otherwise>
                <table class="table table-hover align-middle mb-0">
                    <thead><tr><th>Zona</th><th class="text-end">Asientos activos</th><th class="text-end">Precio</th></tr></thead>
                    <tbody>
                    <c:forEach var="p" items="${precios}">
                        <tr>
                            <td><c:out value="${p.zona}" /></td>
                            <td class="text-end">${p.asientosActivos}</td>
                            <td class="text-end">${empty p.precio ? 'Sin precio' : p.precioTexto}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <c:if test="${evento.estado.editable}">
                    <p class="small text-body-secondary mt-2 mb-0">Los precios se cambian en
                        <a href="${ctx}/organizador/eventos?accion=editar&id=${evento.id}">Editar datos</a>.</p>
                </c:if>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- Ocupación por zona (solo con mapa generado) --%>
    <c:if test="${not empty ocupacion}">
        <section class="card card-body shadow-sm mb-4 table-responsive">
            <h2 class="h5 mb-3">Ocupación</h2>
            <table class="table table-hover align-middle mb-0">
                <thead>
                <tr><th>Zona</th><th class="text-end">Total</th><th class="text-end">Vendidos</th>
                    <th class="text-end">Reservados</th><th class="text-end">Disponibles</th>
                    <th class="text-end">Bloqueados</th><th>% vendido</th></tr>
                </thead>
                <tbody>
                <c:forEach var="o" items="${ocupacion}">
                    <tr>
                        <td><c:out value="${o.zona}" /></td>
                        <td class="text-end">${o.total}</td>
                        <td class="text-end">${o.vendidos}</td>
                        <td class="text-end">${o.reservados}</td>
                        <td class="text-end">${o.disponibles}</td>
                        <td class="text-end">${o.bloqueados}</td>
                        <td>${o.porcentaje}% <div class="progress mt-1" role="progressbar" style="height:8px"><div class="progress-bar bg-success" style="width:${o.porcentaje}%"></div></div></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </section>
    </c:if>

    <%-- Acciones de estado (RF-02, RF-07, RN-09) --%>
    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">Acciones</h2>
        <div class="d-flex flex-wrap gap-2 align-items-center">
            <c:if test="${evento.estado == 'BORRADOR'}">
                <form method="post" action="${ctx}/organizador/eventos" class="d-inline"
                      data-confirmar="¿Publicar el evento?" data-detalle="Se generará el mapa de asientos y quedará a la venta." data-boton="Sí, publicar">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="publicar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="btn btn-acento">Publicar evento</button>
                </form>
            </c:if>
            <c:if test="${not empty siguienteEstado}">
                <form method="post" action="${ctx}/organizador/eventos" class="d-inline"
                      data-confirmar="¿Cambiar el estado a ${siguienteEstado.bd}?" data-boton="Sí, cambiar">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="avanzar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="btn btn-primary">Marcar como ${siguienteEstado.bd}</button>
                </form>
            </c:if>
            <c:if test="${evento.estado.cancelable}">
                <form method="post" action="${ctx}/organizador/eventos" class="d-inline"
                      data-confirmar="¿Cancelar el evento?" data-detalle="Se anularán todos sus boletos (reembolso simulado). Esta acción no se puede deshacer." data-boton="Sí, cancelar evento" data-peligro="si">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="cancelar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="btn btn-danger">Cancelar evento</button>
                </form>
            </c:if>
            <%-- Borrado lógico (RN-10): solo Borrador o Cancelado. --%>
            <c:if test="${evento.estado == 'BORRADOR' or evento.estado == 'CANCELADO'}">
                <form method="post" action="${ctx}/organizador/eventos" class="d-inline"
                      data-confirmar="¿Desactivar el evento?" data-detalle="Dejará de aparecer en el listado y en los reportes." data-boton="Sí, desactivar" data-peligro="si">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="desactivar">
                    <input type="hidden" name="id" value="${evento.id}">
                    <button type="submit" class="btn btn-secondary">Desactivar</button>
                </form>
            </c:if>
            <c:if test="${evento.estado == 'FINALIZADO' or evento.estado == 'CANCELADO'}">
                <span class="small text-body-secondary">El evento está ${evento.estado.bd.toLowerCase()}: solo se puede consultar.</span>
            </c:if>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
