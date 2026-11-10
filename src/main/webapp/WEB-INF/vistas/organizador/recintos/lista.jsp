<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Recintos" />
<c:set var="paginaActiva" value="recintos" />
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
            <h1 class="h3 mb-1">Recintos</h1>
            <p class="text-body-secondary mb-0">Lugares físicos con sus zonas y asientos numerados.</p>
        </div>
        <a class="btn btn-success" href="${ctx}/organizador/recintos?accion=nuevo">Nuevo recinto</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr>
                <th>Nombre</th><th>Tipo</th><th>Dirección</th>
                <th class="text-end">Zonas</th><th class="text-end">Asientos activos</th>
                <th>Estado</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="r" items="${recintos}">
                <tr>
                    <td><strong><c:out value="${r.nombre}" /></strong></td>
                    <td><c:out value="${r.tipo}" /></td>
                    <td><c:out value="${r.direccion}" /></td>
                    <td class="text-end">${r.totalZonas}</td>
                    <td class="text-end">${r.totalAsientos}</td>
                    <td><span class="badge rounded-pill etiqueta ${r.activo ? 'activo' : 'inactivo'}">${r.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <a class="btn btn-primary btn-sm" href="${ctx}/organizador/zonas?recinto=${r.id}">Zonas y asientos</a>
                            <a class="btn btn-warning btn-sm" href="${ctx}/organizador/recintos?accion=editar&id=${r.id}">Editar</a>
                            <form method="post" action="${ctx}/organizador/recintos" class="d-inline">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="estado">
                                <input type="hidden" name="id" value="${r.id}">
                                <button type="submit" class="btn btn-sm ${r.activo ? 'btn-danger' : 'btn-success'}">${r.activo ? 'Desactivar' : 'Activar'}</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty recintos}">
                <tr><td colspan="7" class="text-center text-body-secondary py-4">Todavía no hay recintos registrados.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
