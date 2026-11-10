<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Zonas y asientos" />
<c:set var="paginaActiva" value="recintos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/organizador/recintos">Recintos</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${recinto.nombre}" /></li></ol></nav>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1"><c:out value="${recinto.nombre}" /></h1>
            <p class="text-body-secondary mb-0"><c:out value="${recinto.tipo}" /> · ${recinto.totalZonas} zonas ·
                ${recinto.totalAsientos} asientos activos</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Nueva zona: nombre, descripción y distribución de asientos en un solo paso --%>
    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">Agregar zona</h2>
        <form method="post" action="${ctx}/organizador/zonas" class="row g-2 align-items-end mb-3">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="recinto" value="${recinto.id}">
            <div class="col-md">
                <label class="form-label">Nombre</label>
                <input class="form-control" type="text" name="nombre" maxlength="60" required placeholder="Platea, Palco, Gradas...">
            </div>
            <div class="col-md">
                <label class="form-label">Descripción</label>
                <input class="form-control" type="text" name="descripcion" maxlength="200" placeholder="Opcional">
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label">Fila desde</label>
                <input class="form-control" type="text" name="desde" maxlength="1" required placeholder="A">
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label">Fila hasta</label>
                <input class="form-control" type="text" name="hasta" maxlength="1" required placeholder="J">
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label">Asientos por fila</label>
                <input class="form-control" type="number" name="porFila" min="1" max="50" required placeholder="20">
            </div>
            <div class="col-auto"><button type="submit" class="btn btn-success">Agregar zona</button></div>
        </form>
    </section>

    <div class="leyenda">
        <span><i class="Activo"></i> Asiento activo</span>
        <span><i class="Inactivo"></i> Inhabilitado</span>
    </div>

    <c:forEach var="z" items="${zonas}">
        <section class="card card-body shadow-sm mb-4 zona-mapa">
            <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-2">
                <div>
                    <h3><c:out value="${z.nombre}" /></h3>
                    <span class="small text-body-secondary">${z.asientosActivos} activos de ${z.totalAsientos}</span>
                </div>
                <c:if test="${z.totalAsientos == 0}">
                    <form method="post" action="${ctx}/organizador/zonas" class="d-inline"
                          data-confirmar="¿Eliminar esta zona?" data-boton="Sí, eliminar" data-peligro="si">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="recinto" value="${recinto.id}">
                        <input type="hidden" name="id" value="${z.id}">
                        <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                    </form>
                </c:if>
            </div>

            <%-- Un solo formulario para los datos de la zona y su distribución --%>
            <form method="post" action="${ctx}/organizador/zonas" class="row g-2 align-items-end mb-3">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="guardar">
                <input type="hidden" name="recinto" value="${recinto.id}">
                <input type="hidden" name="id" value="${z.id}">
                <div class="col-md">
                    <label class="form-label">Nombre</label>
                    <input class="form-control" type="text" name="nombre" maxlength="60" required value="<c:out value='${z.nombre}' />">
                </div>
                <div class="col-md">
                    <label class="form-label">Descripción</label>
                    <input class="form-control" type="text" name="descripcion" maxlength="200" value="<c:out value='${z.descripcion}' />">
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label">Fila desde</label>
                    <input class="form-control" type="text" name="desde" maxlength="1" required placeholder="A" value="${z.filaDesde}">
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label">Fila hasta</label>
                    <input class="form-control" type="text" name="hasta" maxlength="1" required placeholder="J" value="${z.filaHasta}">
                </div>
                <div class="col-6 col-md-2">
                    <label class="form-label">Asientos por fila</label>
                    <input class="form-control" type="number" name="porFila" min="1" max="50" required placeholder="20" value="${z.asientosPorFila}">
                </div>
                <div class="col-auto"><button type="submit" class="btn btn-primary">Guardar zona</button></div>
            </form>

            <c:choose>
                <c:when test="${empty z.filas}">
                    <p class="small text-body-secondary">Esta zona todavía no tiene asientos: indique las filas y los asientos por fila y pulse Guardar zona.</p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/organizador/zonas" class="filas">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="asiento">
                        <input type="hidden" name="recinto" value="${recinto.id}">
                        <c:forEach var="fila" items="${z.filas}">
                            <div class="fila-asientos">
                                <span class="letra">${fila.letra}</span>
                                <c:forEach var="a" items="${fila.asientos}">
                                    <button type="submit" name="cambio" value="${a.id}:${a.activo ? 'no' : 'si'}"
                                            class="asiento ${a.clasePlantilla}"
                                            title="${a.etiqueta} — ${a.activo ? 'clic para inhabilitar' : 'clic para habilitar'}">${a.numero}</button>
                                </c:forEach>
                            </div>
                        </c:forEach>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>
    </c:forEach>
    <c:if test="${empty zonas}">
        <div class="alert alert-info">Este recinto todavía no tiene zonas. Agregue la primera con el formulario de arriba.</div>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
