<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Categorías" />
<c:set var="paginaActiva" value="categorias" />
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
            <h1 class="h3 mb-1">Categorías</h1>
            <p class="text-body-secondary mb-0">Clasifican los eventos en la cartelera (Rock, Ópera, Fútbol...).</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">${empty categoria.id ? 'Nueva categoría' : 'Editar categoría'}</h2>
        <form method="post" action="${ctx}/organizador/categorias" class="filtros d-flex flex-row flex-wrap gap-3 align-items-end">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${categoria.id}">
            <div class="campo-filtro" style="min-width:280px">
                <label class="form-label" for="nombre">Nombre</label>
                <input class="form-control" type="text" id="nombre" name="nombre" maxlength="60" required
                       value="<c:out value='${categoria.nombre}' />">
            </div>
            <button type="submit" class="btn btn-success">Guardar</button>
            <c:if test="${not empty categoria.id}">
                <a class="btn btn-secondary" href="${ctx}/organizador/categorias">Cancelar</a>
            </c:if>
        </form>
    </section>

    <section class="card card-body shadow-sm mb-4 table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr><th>Nombre</th><th class="text-end">Eventos</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <c:forEach var="cat" items="${categorias}">
                <tr>
                    <td><c:out value="${cat.nombre}" /></td>
                    <td class="text-end">${cat.totalEventos}</td>
                    <td>
                        <div class="d-flex flex-wrap gap-2 align-items-center">
                            <a class="btn btn-warning btn-sm" href="${ctx}/organizador/categorias?accion=editar&id=${cat.id}">Editar</a>
                            <c:if test="${cat.totalEventos == 0}">
                                <form method="post" action="${ctx}/organizador/categorias" class="d-inline"
                                      data-confirmar="¿Eliminar esta categoría?" data-boton="Sí, eliminar" data-peligro="si">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${cat.id}">
                                    <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
                                </form>
                            </c:if>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty categorias}">
                <tr><td colspan="3" class="text-center text-body-secondary py-4">Todavía no hay categorías.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
