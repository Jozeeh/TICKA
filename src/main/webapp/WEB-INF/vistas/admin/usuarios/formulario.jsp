<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${empty usuario.id ? 'Nuevo usuario' : 'Editar usuario'}" />
<c:set var="paginaActiva" value="usuarios" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/admin/usuarios">Usuarios</a> › <c:out value="${titulo}" /></div>
    <h1><c:out value="${titulo}" /></h1>
    <p class="subtitulo">Las contraseñas se guardan con hash BCrypt; nadie puede verlas, solo restablecerlas.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta formulario">
        <form method="post" action="${ctx}/admin/usuarios" autocomplete="off">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${usuario.id}">
            <div class="rejilla-campos">
                <div class="grupo-campo">
                    <label for="username">Usuario</label>
                    <c:choose>
                        <c:when test="${empty usuario.id}">
                            <input type="text" id="username" name="username" maxlength="30" required
                                   value="<c:out value='${usuario.username}' />">
                            <span class="ayuda">Minúsculas, números, punto, guion o guion bajo (4 a 30).</span>
                        </c:when>
                        <c:otherwise>
                            <input type="text" id="username" value="<c:out value='${usuario.username}' />" disabled>
                            <span class="ayuda">El usuario no se puede cambiar.</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="grupo-campo">
                    <label for="nombre">Nombre completo</label>
                    <input type="text" id="nombre" name="nombre" maxlength="100" required
                           value="<c:out value='${usuario.nombre}' />">
                </div>
                <div class="grupo-campo">
                    <label for="rol">Rol</label>
                    <select id="rol" name="rol" required>
                        <c:forEach var="r" items="${roles}">
                            <option value="${r}" ${usuario.rol == r ? 'selected' : ''}>${r.nombre}</option>
                        </c:forEach>
                    </select>
                </div>
                <c:if test="${not empty usuario.id}">
                    <div class="grupo-campo">
                        <label>&nbsp;</label>
                        <label class="casilla">
                            <input type="checkbox" name="activo" value="si" ${usuario.activo ? 'checked' : ''}>
                            Cuenta activa
                        </label>
                    </div>
                </c:if>
                <div class="grupo-campo">
                    <label for="clave">${empty usuario.id ? 'Contraseña' : 'Nueva contraseña (opcional)'}</label>
                    <input type="password" id="clave" name="clave" autocomplete="new-password" ${empty usuario.id ? 'required' : ''}>
                    <span class="ayuda">Mínimo 8 caracteres, con letras y números.</span>
                </div>
                <div class="grupo-campo">
                    <label for="confirmacion">Confirmar contraseña</label>
                    <input type="password" id="confirmacion" name="confirmacion" autocomplete="new-password" ${empty usuario.id ? 'required' : ''}>
                </div>
            </div>
            <div class="acciones-formulario">
                <button type="submit" class="boton verde">Guardar</button>
                <a class="boton gris" href="${ctx}/admin/usuarios">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
