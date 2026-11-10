<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Incidencias" />
<c:set var="paginaActiva" value="incidencias" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <h1 class="h3 mb-1">Incidencias</h1>
    <p class="text-body-secondary mb-4">Registre los incidentes del evento y deles seguimiento: Abierta → En atención → Cerrada.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Registrar (RF-32) --%>
    <section class="card card-body shadow-sm mb-4 formulario">
        <h2 class="h5 mb-3">Nueva incidencia</h2>
        <form method="post" action="${ctx}/puerta/incidencias">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="registrar">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="nEvento">Evento</label>
                    <select class="form-select" id="nEvento" name="evento" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="ev" items="${eventos}">
                            <option value="${ev.id}" ${eventoTrabajo == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /> · ${ev.fechaTexto}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="nTipo">Tipo</label>
                    <select class="form-select" id="nTipo" name="tipo" required>
                        <c:forEach var="t" items="${tipos}"><option value="${t[0]}">${t[1]}</option></c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="nGravedad">Gravedad</label>
                    <select class="form-select" id="nGravedad" name="gravedad" required>
                        <c:forEach var="g" items="${gravedades}"><option value="${g}" ${g == 'Media' ? 'selected' : ''}>${g}</option></c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="nPuerta">Puerta o ubicación</label>
                    <input class="form-control" type="text" id="nPuerta" name="puerta" maxlength="20" value="<c:out value='${puertaTrabajo}' />">
                </div>
                <div class="col-12">
                    <label class="form-label" for="nDescripcion">Descripción</label>
                    <textarea class="form-control" id="nDescripcion" name="descripcion" required minlength="5" maxlength="500"></textarea>
                </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3"><button type="submit" class="btn btn-success">Registrar incidencia</button></div>
        </form>
    </section>

    <%-- Consultar (RF-34) --%>
    <form method="get" action="${ctx}/puerta/incidencias" class="card card-body shadow-sm mb-4 filtros d-flex flex-row flex-wrap gap-3 align-items-end">
        <div class="campo-filtro" style="min-width:240px">
            <label class="form-label" for="evento">Evento</label>
            <select class="form-select" id="evento" name="evento">
                <option value="">Todos</option>
                <c:forEach var="ev" items="${eventosFiltro}">
                    <option value="${ev.id}" ${idEvento == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="tipo">Tipo</label>
            <select class="form-select" id="tipo" name="tipo">
                <option value="">Todos</option>
                <c:forEach var="t" items="${tipos}"><option value="${t[0]}" ${tipoFiltro == t[0] ? 'selected' : ''}>${t[1]}</option></c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="gravedad">Gravedad</label>
            <select class="form-select" id="gravedad" name="gravedad">
                <option value="">Todas</option>
                <c:forEach var="g" items="${gravedades}"><option value="${g}" ${gravedadFiltro == g ? 'selected' : ''}>${g}</option></c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="estado">Estado</label>
            <select class="form-select" id="estado" name="estado">
                <option value="">Todos</option>
                <c:forEach var="e" items="${estados}"><option value="${e}" ${estadoFiltro == e ? 'selected' : ''}>${e == 'En atencion' ? 'En atención' : e}</option></c:forEach>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">Filtrar</button>
        <a class="btn btn-secondary" href="${ctx}/puerta/incidencias">Limpiar</a>
    </form>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead><tr><th>Fecha</th><th>Evento</th><th>Tipo</th><th>Gravedad</th><th>Descripción</th><th>Estado</th><th>Seguimiento</th></tr></thead>
            <tbody>
            <c:forEach var="i" items="${incidencias}">
                <tr>
                    <td>${i.fechaHoraTexto}<br><span class="small text-body-secondary"><c:out value="${i.puerta}" /></span></td>
                    <td><c:out value="${i.evento}" /></td>
                    <td><c:out value="${i.tipoTexto}" /></td>
                    <td><span class="badge rounded-pill etiqueta ${i.gravedad == 'Alta' ? 'inactivo' : i.gravedad == 'Media' ? 'bloqueado' : 'Borrador'}">${i.gravedad}</span></td>
                    <td style="max-width:320px"><c:out value="${i.descripcion}" />
                        <c:if test="${not empty i.observaciones}"><br><span class="small text-body-secondary">Obs.: <c:out value="${i.observaciones}" /></span></c:if></td>
                    <td><span class="badge rounded-pill etiqueta ${i.estado == 'Cerrada' ? 'activo' : i.estado == 'Abierta' ? 'inactivo' : 'Encurso'}">${i.estadoTexto}</span></td>
                    <td>
                        <c:if test="${i.estado != 'Cerrada'}">
                            <form method="post" action="${ctx}/puerta/incidencias" class="d-flex flex-wrap gap-2 align-items-center">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="estado">
                                <input type="hidden" name="id" value="${i.id}">
                                <input class="form-control form-control-sm" type="text" name="observaciones" maxlength="500" placeholder="Observaciones" style="width:150px">
                                <c:if test="${i.estado == 'Abierta'}">
                                    <button type="submit" name="estado" value="En atencion" class="btn btn-primary btn-sm">En atención</button>
                                </c:if>
                                <button type="submit" name="estado" value="Cerrada" class="btn btn-success btn-sm">Cerrar</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty incidencias}">
                <tr><td colspan="7" class="text-center text-body-secondary py-4">No hay incidencias con esos filtros.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
