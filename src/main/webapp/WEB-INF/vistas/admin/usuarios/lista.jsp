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
            <p class="subtitulo">Organizadores, personal de control de acceso y administradores.</p>
        </div>
        <a class="boton verde" href="${ctx}/admin/usuarios?accion=nuevo">Nuevo usuario</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

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
                    <td class="acciones-linea">
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
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty usuarios}">
                <tr><td colspan="6" class="vacio">No hay usuarios del personal.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
