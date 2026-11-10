<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Usuarios" />
<c:set var="paginaActiva" value="usuarios" />
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
            <h1 class="h3 mb-1">Usuarios del personal</h1>
            <p class="text-body-secondary mb-0">Organizadores, personal de control de acceso y administradores.<c:if test="${not empty paginacion}"> ${paginacion.total} usuarios encontrados.</c:if></p>
        </div>
        <a class="btn btn-success" href="${ctx}/admin/usuarios?accion=nuevo">Nuevo usuario</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Búsqueda y filtro por rol (RF-55) --%>
    <form method="get" action="${ctx}/admin/usuarios" class="card card-body shadow-sm mb-4 filtros d-flex flex-row flex-wrap gap-3 align-items-end">
        <div class="campo-filtro" style="min-width:280px">
            <label class="form-label" for="texto">Buscar</label>
            <input class="form-control" type="text" id="texto" name="texto" placeholder="Nombre o usuario" value="<c:out value='${texto}' />">
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="rol">Rol</label>
            <select class="form-select" id="rol" name="rol">
                <option value="">Todos</option>
                <c:forEach var="r" items="${roles}">
                    <option value="${r}" ${rolFiltro == r ? 'selected' : ''}><c:out value="${r.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="btn btn-primary">Buscar</button>
        <a class="btn btn-secondary" href="${ctx}/admin/usuarios">Limpiar</a>
    </form>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr><th>Nombre</th><th>Usuario</th><th>Rol</th><th>Estado</th><th>Creado</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <c:forEach var="us" items="${usuarios}">
                <tr>
                    <td><strong><c:out value="${us.nombre}" /></strong>
                        <c:if test="${us.id == u.id}"><span class="small text-body-secondary">(usted)</span></c:if></td>
                    <td><c:out value="${us.username}" /></td>
                    <td><c:out value="${us.rol.nombre}" /></td>
                    <td>
                        <span class="badge rounded-pill etiqueta ${us.activo ? 'activo' : 'inactivo'}">${us.activo ? 'Activo' : 'Inactivo'}</span>
                        <c:if test="${us.bloqueado}">
                            <span class="badge rounded-pill etiqueta bloqueado" title="Hasta ${us.bloqueadoHastaTexto}">Bloqueado</span>
                        </c:if>
                    </td>
                    <td>${us.creadoEnTexto}</td>
                    <td>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <a class="btn btn-warning btn-sm" href="${ctx}/admin/usuarios?accion=editar&id=${us.id}">Editar</a>
                            <c:if test="${us.bloqueado}">
                                <form method="post" action="${ctx}/admin/usuarios" class="d-inline">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="desbloquear">
                                    <input type="hidden" name="id" value="${us.id}">
                                    <button type="submit" class="btn btn-primary btn-sm">Desbloquear</button>
                                </form>
                            </c:if>
                            <c:if test="${us.id != u.id}">
                                <form method="post" action="${ctx}/admin/usuarios" class="d-inline">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${us.id}">
                                    <input type="hidden" name="activar" value="${us.activo ? 'no' : 'si'}">
                                    <button type="submit" class="btn btn-sm ${us.activo ? 'btn-danger' : 'btn-success'}">${us.activo ? 'Desactivar' : 'Activar'}</button>
                                </form>
                            </c:if>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty usuarios}">
                <tr><td colspan="6" class="text-center text-body-secondary py-4">No hay usuarios con esos criterios.</td></tr>
            </c:if>
            </tbody>
        </table>
        <c:if test="${paginacion.paginas > 1}">
            <nav class="d-flex flex-wrap align-items-center gap-3 mt-3" aria-label="Paginación">
                <ul class="pagination pagination-sm mb-0">
                <c:forEach var="p" begin="1" end="${paginacion.paginas}">
                    <c:url var="enlace" value="/admin/usuarios">
                        <c:param name="texto" value="${texto}" />
                        <c:param name="rol" value="${rolFiltro}" />
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
