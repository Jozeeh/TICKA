<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Mi perfil" />
<c:set var="paginaActiva" value="perfil" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <h1 class="h3 mb-1">Mi perfil</h1>
    <p class="text-body-secondary mb-4">Sus datos de acceso. El usuario y el rol solo los puede cambiar un administrador.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="card card-body shadow-sm mb-4 formulario">
        <h2 class="h5 mb-3">Datos de la cuenta</h2>
        <form method="post" action="${ctx}/perfil">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="datos">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Usuario</label>
                    <input class="form-control" type="text" value="<c:out value='${cuenta.username}' />" disabled>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Rol</label>
                    <input class="form-control" type="text" value="<c:out value='${cuenta.rol.nombre}' />" disabled>
                </div>
                <div class="col-12">
                    <label class="form-label" for="nombre">Nombre completo</label>
                    <input class="form-control" type="text" id="nombre" name="nombre" maxlength="100" required value="<c:out value='${cuenta.nombre}' />">
                </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3"><button type="submit" class="btn btn-success">Guardar datos</button></div>
        </form>
    </section>

    <section class="card card-body shadow-sm mb-4 formulario">
        <h2 class="h5 mb-3">Cambiar contraseña</h2>
        <form method="post" action="${ctx}/perfil" autocomplete="off">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="clave">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="actual">Contraseña actual</label>
                    <input class="form-control" type="password" id="actual" name="actual" required autocomplete="current-password">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="nueva">Nueva contraseña</label>
                    <input class="form-control" type="password" id="nueva" name="nueva" required minlength="8" autocomplete="new-password">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="confirmacion">Confirmar nueva contraseña</label>
                    <input class="form-control" type="password" id="confirmacion" name="confirmacion" required minlength="8" autocomplete="new-password">
                </div>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-3"><button type="submit" class="btn btn-primary">Cambiar contraseña</button></div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
