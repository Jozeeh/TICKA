<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Clientes" />
<c:set var="paginaActiva" value="clientes" />
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
            <h1 class="h3 mb-1">Clientes</h1>
            <p class="text-body-secondary mb-0">Cuentas creadas por autorregistro en el portal. Aquí se consultan con sus compras y se activan o desactivan.<c:if test="${not empty paginacion}"> ${paginacion.total} clientes encontrados.</c:if></p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <form method="get" action="${ctx}/admin/clientes" class="card card-body shadow-sm mb-4 filtros d-flex flex-row flex-wrap gap-3 align-items-end">
        <div class="campo-filtro" style="min-width:320px">
            <label class="form-label" for="texto">Buscar</label>
            <input class="form-control" type="text" id="texto" name="texto" placeholder="Nombre, documento, correo o usuario"
                   value="<c:out value='${texto}' />">
        </div>
        <button type="submit" class="btn btn-primary">Buscar</button>
        <a class="btn btn-secondary" href="${ctx}/admin/clientes">Limpiar</a>
    </form>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr>
                <th>Cliente</th><th>Usuario</th><th>Documento</th><th>Correo</th><th>Teléfono</th>
                <th class="text-end">Compras</th><th class="text-end">Boletos</th><th class="text-end">Total</th>
                <th>Estado</th><th>Acción</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="cl" items="${clientes}">
                <tr>
                    <td><strong><c:out value="${cl.nombreCompleto}" /></strong></td>
                    <td><c:out value="${cl.username}" /></td>
                    <td><c:out value="${cl.documento}" /></td>
                    <td><c:out value="${cl.correo}" /></td>
                    <td><c:out value="${cl.telefono}" /></td>
                    <td class="text-end">${cl.compras}</td>
                    <td class="text-end">${cl.boletos}</td>
                    <td class="text-end">${cl.totalGastadoTexto}</td>
                    <td><span class="badge rounded-pill etiqueta ${cl.activo ? 'activo' : 'inactivo'}">${cl.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                        <a class="btn btn-primary btn-sm" href="${ctx}/admin/clientes?accion=ver&id=${cl.id}">Ver</a>
                        <form method="post" action="${ctx}/admin/clientes" class="d-inline">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="usuario" value="${cl.idUsuario}">
                            <input type="hidden" name="activar" value="${cl.activo ? 'no' : 'si'}">
                            <button type="submit" class="btn btn-sm ${cl.activo ? 'btn-danger' : 'btn-success'}">${cl.activo ? 'Desactivar' : 'Activar'}</button>
                        </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty clientes}">
                <tr><td colspan="10" class="text-center text-body-secondary py-4">No se encontraron clientes.</td></tr>
            </c:if>
            </tbody>
        </table>
        <%-- Paginación (RF-55): conserva la búsqueda --%>
        <c:if test="${paginacion.paginas > 1}">
            <nav class="d-flex flex-wrap align-items-center gap-3 mt-3" aria-label="Paginación">
                <ul class="pagination pagination-sm mb-0">
                <c:forEach var="p" begin="1" end="${paginacion.paginas}">
                    <c:url var="enlace" value="/admin/clientes">
                        <c:param name="texto" value="${texto}" />
                        <c:param name="pagina" value="${p}" />
                    </c:url>
                    <li class="page-item ${p == paginacion.pagina ? 'active' : ''}"><a class="page-link" href="${enlace}">${p}</a></li>
                </c:forEach>
                </ul>
                <span class="small text-body-secondary">Página ${paginacion.pagina} de ${paginacion.paginas}</span>
            </nav>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
