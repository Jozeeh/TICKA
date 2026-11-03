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
        <h1>Crear una cuenta</h1>
        <p class="acceso-sub">¿Ya tienes cuenta? <a href="${pageContext.request.contextPath}/acceso">Iniciar sesión</a></p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

        <%-- Autorregistro del cliente (RF-13). Documento y correo no se pueden repetir (RF-14). --%>
        <form method="post" action="${pageContext.request.contextPath}/registro" autocomplete="on">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="acceso-par">
                <div>
                    <label for="nombres" class="solo-lector">Nombres</label>
                    <input type="text" id="nombres" name="nombres" maxlength="80" required placeholder="Nombres"
                           autocomplete="given-name" value="<c:out value='${cliente.nombres}' />">
                </div>
                <div>
                    <label for="apellidos" class="solo-lector">Apellidos</label>
                    <input type="text" id="apellidos" name="apellidos" maxlength="80" required placeholder="Apellidos"
                           autocomplete="family-name" value="<c:out value='${cliente.apellidos}' />">
                </div>
            </div>
            <div class="acceso-par">
                <div>
                    <label for="documento" class="solo-lector">Documento (DUI o pasaporte)</label>
                    <input type="text" id="documento" name="documento" maxlength="20" required
                           placeholder="DUI o pasaporte" value="<c:out value='${cliente.documento}' />">
                </div>
                <div>
                    <label for="telefono" class="solo-lector">Teléfono (opcional)</label>
                    <input type="tel" id="telefono" name="telefono" maxlength="20" autocomplete="tel"
                           placeholder="Teléfono (opcional)" value="<c:out value='${cliente.telefono}' />">
                </div>
            </div>
            <label for="correo" class="solo-lector">Correo electrónico</label>
            <input type="email" id="correo" name="correo" maxlength="120" required autocomplete="email"
                   placeholder="Correo electrónico" value="<c:out value='${cliente.correo}' />">

            <label for="usuario" class="solo-lector">Usuario</label>
            <input type="text" id="usuario" name="usuario" maxlength="30" required autocomplete="username"
                   placeholder="Usuario (minúsculas, números, punto o guion)" value="<c:out value='${usuario}' />">

            <div class="acceso-par">
                <div>
                    <label for="clave" class="solo-lector">Contraseña</label>
                    <div class="campo-clave">
                        <input type="password" id="clave" name="clave" required minlength="8"
                               autocomplete="new-password" placeholder="Contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
                <div>
                    <label for="confirmacion" class="solo-lector">Confirmar contraseña</label>
                    <div class="campo-clave">
                        <input type="password" id="confirmacion" name="confirmacion" required minlength="8"
                               autocomplete="new-password" placeholder="Confirmar contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
            </div>
            <p class="acceso-nota">La contraseña debe tener al menos 8 caracteres, con letras y números.</p>
            <button type="submit" class="boton acento acento-grande">Crear cuenta</button>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/clave-visible.jspf" %>
</body>
</html>
