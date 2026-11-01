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
<main class="contenedor">
    <h1>Mi perfil</h1>
    <p class="subtitulo">Sus datos de cliente. El documento no se puede cambiar.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta formulario" style="max-width:760px">
        <h2 style="margin-top:0">Datos personales</h2>
        <form method="post" action="${ctx}/cliente/perfil">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="datos">
            <div class="rejilla-campos">
                <div class="grupo-campo">
                    <label for="nombres">Nombres</label>
                    <input type="text" id="nombres" name="nombres" maxlength="80" required value="<c:out value='${cliente.nombres}' />">
                </div>
                <div class="grupo-campo">
                    <label for="apellidos">Apellidos</label>
                    <input type="text" id="apellidos" name="apellidos" maxlength="80" required value="<c:out value='${cliente.apellidos}' />">
                </div>
                <div class="grupo-campo">
                    <label>Documento</label>
                    <input type="text" value="<c:out value='${cliente.documento}' />" disabled>
                </div>
                <div class="grupo-campo">
                    <label for="telefono">Teléfono</label>
                    <input type="tel" id="telefono" name="telefono" maxlength="20" value="<c:out value='${cliente.telefono}' />">
                </div>
                <div class="grupo-campo completo">
                    <label for="correo">Correo electrónico</label>
                    <input type="email" id="correo" name="correo" maxlength="120" required value="<c:out value='${cliente.correo}' />">
                </div>
            </div>
            <div class="acciones-formulario"><button type="submit" class="boton verde">Guardar datos</button></div>
        </form>
    </section>

    <section class="tarjeta formulario" style="max-width:760px">
        <h2 style="margin-top:0">Cambiar contraseña</h2>
        <form method="post" action="${ctx}/cliente/perfil" autocomplete="off">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="clave">
            <div class="rejilla-campos">
                <div class="grupo-campo completo">
                    <label for="actual">Contraseña actual</label>
                    <input type="password" id="actual" name="actual" required autocomplete="current-password">
                </div>
                <div class="grupo-campo">
                    <label for="nueva">Nueva contraseña</label>
                    <input type="password" id="nueva" name="nueva" required minlength="8" autocomplete="new-password">
                </div>
                <div class="grupo-campo">
                    <label for="confirmacion">Confirmar nueva contraseña</label>
                    <input type="password" id="confirmacion" name="confirmacion" required minlength="8" autocomplete="new-password">
                </div>
            </div>
            <div class="acciones-formulario"><button type="submit" class="boton">Cambiar contraseña</button></div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
