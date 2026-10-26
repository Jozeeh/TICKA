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
<main class="contenedor">
    <div class="cabecera-pagina">
        <div>
            <h1>Usuarios del personal</h1>
            <p class="subtitulo">Organizadores, personal de control de acceso y administradores.<c:if test="${not empty paginacion}"> ${paginacion.total} usuarios encontrados.</c:if></p>
        </div>
        <a class="boton verde" href="${ctx}/admin/usuarios?accion=nuevo">Nuevo usuario</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Búsqueda y filtro por rol (RF-55) --%>
    <form method="get" action="${ctx}/admin/usuarios" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:280px">
            <label for="texto">Buscar</label>
            <input type="text" id="texto" name="texto" placeholder="Nombre o usuario" value="<c:out value='${texto}' />">
        </div>
        <div class="grupo-campo">
            <label for="rol">Rol</label>
            <select id="rol" name="rol">
                <option value="">Todos</option>
                <c:forEach var="r" items="${roles}">
                    <option value="${r}" ${rolFiltro == r ? 'selected' : ''}><c:out value="${r.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="boton">Buscar</button>
        <a class="boton gris" href="${ctx}/admin/usuarios">Limpiar</a>
    </form>

    <section class="tarjeta tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr><th>Nombre</th><th>Usuario</th><th>Rol</th><th>Estado</th><th>Creado</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <c:forEach var="us" items="${usuarios}">
                <tr>
                    <td><strong><c:out value="${us.nombre}" /></strong>
                        <c:if test="${us.id == u.id}"><span class="ayuda">(usted)</span></c:if></td>
                    <td><c:out value="${us.username}" /></td>
                    <td><c:out value="${us.rol.nombre}" /></td>
                    <td>
                        <span class="etiqueta ${us.activo ? 'activo' : 'inactivo'}">${us.activo ? 'Activo' : 'Inactivo'}</span>
                        <c:if test="${us.bloqueado}">
                            <span class="etiqueta bloqueado" title="Hasta ${us.bloqueadoHastaTexto}">Bloqueado</span>
                        </c:if>
                    </td>
                    <td>${us.creadoEnTexto}</td>
                    <td>
                        <div class="acciones-linea">
                            <a class="boton amarillo chico" href="${ctx}/admin/usuarios?accion=editar&id=${us.id}">Editar</a>
                            <c:if test="${us.bloqueado}">
                                <form method="post" action="${ctx}/admin/usuarios" class="linea">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="desbloquear">
                                    <input type="hidden" name="id" value="${us.id}">
                                    <button type="submit" class="boton chico">Desbloquear</button>
                                </form>
                            </c:if>
                            <c:if test="${us.id != u.id}">
                                <form method="post" action="${ctx}/admin/usuarios" class="linea">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${us.id}">
                                    <input type="hidden" name="activar" value="${us.activo ? 'no' : 'si'}">
                                    <button type="submit" class="boton chico ${us.activo ? 'rojo' : 'verde'}">${us.activo ? 'Desactivar' : 'Activar'}</button>
                                </form>
                            </c:if>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty usuarios}">
                <tr><td colspan="6" class="vacio">No hay usuarios con esos criterios.</td></tr>
            </c:if>
            </tbody>
        </table>
        <c:if test="${paginacion.paginas > 1}">
            <div class="paginacion">
                <c:forEach var="p" begin="1" end="${paginacion.paginas}">
                    <c:url var="enlace" value="/admin/usuarios">
                        <c:param name="texto" value="${texto}" />
                        <c:param name="rol" value="${rolFiltro}" />
                        <c:param name="pagina" value="${p}" />
                    </c:url>
                    <a class="boton chico ${p == paginacion.pagina ? '' : 'borde'}" href="${enlace}">${p}</a>
                </c:forEach>
                <span>Página ${paginacion.pagina} de ${paginacion.paginas}</span>
            </div>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
