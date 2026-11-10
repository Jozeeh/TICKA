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
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/admin/usuarios">Usuarios</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${titulo}" /></li></ol></nav>
    <h1 class="h3 mb-1"><c:out value="${titulo}" /></h1>
    <p class="text-body-secondary mb-4">Las contraseñas se guardan con hash BCrypt; nadie puede verlas, solo restablecerlas.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="card card-body shadow-sm mb-4 formulario">
        <form method="post" action="${ctx}/admin/usuarios" autocomplete="off">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${usuario.id}">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="username">Usuario</label>
                    <c:choose>
                        <c:when test="${empty usuario.id}">
                            <input class="form-control" type="text" id="username" name="username" maxlength="30" required
                                   value="<c:out value='${usuario.username}' />">
                            <span class="small text-body-secondary">Minúsculas, números, punto, guion o guion bajo (4 a 30).</span>
                        </c:when>
                        <c:otherwise>
                            <input class="form-control" type="text" id="username" value="<c:out value='${usuario.username}' />" disabled>
                            <span class="small text-body-secondary">El usuario no se puede cambiar.</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="nombre">Nombre completo</label>
                    <input class="form-control" type="text" id="nombre" name="nombre" maxlength="100" required
                           value="<c:out value='${usuario.nombre}' />">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="rol">Rol</label>
                    <select class="form-select" id="rol" name="rol" required>
                        <c:forEach var="r" items="${roles}">
                            <option value="${r}" ${usuario.rol == r ? 'selected' : ''}>${r.nombre}</option>
                        </c:forEach>
                    </select>
                </div>
                <c:if test="${not empty usuario.id}">
                    <div class="col-md-6">
                        <label class="form-label">&nbsp;</label>
                        <label class="form-check">
                            <input class="form-check-input" type="checkbox" name="activo" value="si" ${usuario.activo ? 'checked' : ''}>
                            Cuenta activa
                        </label>
                    </div>
                </c:if>
                <div class="col-md-6">
                    <label class="form-label" for="clave">${empty usuario.id ? 'Contraseña' : 'Nueva contraseña (opcional)'}</label>
                    <input class="form-control" type="password" id="clave" name="clave" autocomplete="new-password" ${empty usuario.id ? 'required' : ''}>
                    <span class="small text-body-secondary">Mínimo 8 caracteres, con letras y números.</span>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="confirmacion">Confirmar contraseña</label>
                    <input class="form-control" type="password" id="confirmacion" name="confirmacion" autocomplete="new-password" ${empty usuario.id ? 'required' : ''}>
                </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3">
                <button type="submit" class="btn btn-success">Guardar</button>
                <a class="btn btn-secondary" href="${ctx}/admin/usuarios">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
