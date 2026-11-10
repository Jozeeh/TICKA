<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Crear cuenta" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body class="cuerpo-acceso">
<main class="acceso-pantalla">
    <%@ include file="/WEB-INF/vistas/plantilla/acceso-imagen.jspf" %>

    <section class="acceso-formulario">
        <h1 class="h3 mb-1">Crear una cuenta</h1>
        <p class="acceso-sub">¿Ya tienes cuenta? <a href="${pageContext.request.contextPath}/acceso">Iniciar sesión</a></p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

        <%-- Autorregistro del cliente (RF-13). Documento y correo no se pueden repetir (RF-14). --%>
        <form method="post" action="${pageContext.request.contextPath}/registro" autocomplete="on">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="row gx-3">
                <div class="col-sm-6">
                    <label for="nombres" class="visually-hidden">Nombres</label>
                    <input class="form-control" type="text" id="nombres" name="nombres" maxlength="80" required placeholder="Nombres"
                           autocomplete="given-name" value="<c:out value='${cliente.nombres}' />">
                </div>
                <div class="col-sm-6">
                    <label for="apellidos" class="visually-hidden">Apellidos</label>
                    <input class="form-control" type="text" id="apellidos" name="apellidos" maxlength="80" required placeholder="Apellidos"
                           autocomplete="family-name" value="<c:out value='${cliente.apellidos}' />">
                </div>
            </div>
            <div class="row gx-3">
                <div class="col-sm-6">
                    <label for="documento" class="visually-hidden">Documento (DUI o pasaporte)</label>
                    <input class="form-control" type="text" id="documento" name="documento" maxlength="20" required
                           placeholder="DUI o pasaporte" value="<c:out value='${cliente.documento}' />">
                </div>
                <div class="col-sm-6">
                    <label for="telefono" class="visually-hidden">Teléfono (opcional)</label>
                    <input class="form-control" type="tel" id="telefono" name="telefono" maxlength="20" autocomplete="tel"
                           placeholder="Teléfono (opcional)" value="<c:out value='${cliente.telefono}' />">
                </div>
            </div>
            <label for="correo" class="visually-hidden">Correo electrónico</label>
            <input class="form-control" type="email" id="correo" name="correo" maxlength="120" required autocomplete="email"
                   placeholder="Correo electrónico" value="<c:out value='${cliente.correo}' />">

            <label for="usuario" class="visually-hidden">Usuario</label>
            <input class="form-control" type="text" id="usuario" name="usuario" maxlength="30" required autocomplete="username"
                   placeholder="Usuario (minúsculas, números, punto o guion)" value="<c:out value='${usuario}' />">

            <div class="row gx-3">
                <div class="col-sm-6">
                    <label for="clave" class="visually-hidden">Contraseña</label>
                    <div class="campo-clave">
                        <input class="form-control" type="password" id="clave" name="clave" required minlength="8"
                               autocomplete="new-password" placeholder="Contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
                <div class="col-sm-6">
                    <label for="confirmacion" class="visually-hidden">Confirmar contraseña</label>
                    <div class="campo-clave">
                        <input class="form-control" type="password" id="confirmacion" name="confirmacion" required minlength="8"
                               autocomplete="new-password" placeholder="Confirmar contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
            </div>
            <p class="acceso-nota">La contraseña debe tener al menos 8 caracteres, con letras y números.</p>
            <button type="submit" class="btn btn-acento btn-lg w-100">Crear cuenta</button>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/clave-visible.jspf" %>
</body>
</html>
