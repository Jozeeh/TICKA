<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${empty recinto.id ? 'Nuevo recinto' : 'Editar recinto'}" />
<c:set var="paginaActiva" value="recintos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/organizador/recintos">Recintos</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${titulo}" /></li></ol></nav>
    <h1 class="h3 mb-1"><c:out value="${titulo}" /></h1>
    <p class="text-body-secondary mb-4">Después de guardarlo podrá agregar sus zonas y generar los asientos.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="card card-body shadow-sm mb-4 formulario">
        <form method="post" action="${ctx}/organizador/recintos">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${recinto.id}">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="nombre">Nombre</label>
                    <input class="form-control" type="text" id="nombre" name="nombre" maxlength="120" required
                           value="<c:out value='${recinto.nombre}' />">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="tipo">Tipo</label>
                    <select class="form-select" id="tipo" name="tipo" required>
                        <c:forEach var="t" items="${tipos}">
                            <option value="${t}" ${recinto.tipo == t ? 'selected' : ''}>${t}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="direccion">Dirección</label>
                    <input class="form-control" type="text" id="direccion" name="direccion" maxlength="200"
                           value="<c:out value='${recinto.direccion}' />">
                </div>
                <c:if test="${not empty recinto.id}">
                    <div class="col-12">
                        <label class="form-check">
                            <input class="form-check-input" type="checkbox" name="activo" value="si" ${recinto.activo ? 'checked' : ''}>
                            Recinto activo (disponible para nuevos eventos)
                        </label>
                    </div>
                </c:if>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3">
                <button type="submit" class="btn btn-success">Guardar</button>
                <a class="btn btn-secondary" href="${ctx}/organizador/recintos">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
