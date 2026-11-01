<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Crear cuenta" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<main class="pagina-acceso">
    <section class="caja-acceso ancha">
        <div class="marca-grande">TICK<span>A</span></div>
        <p class="lema">Cree su cuenta para comprar boletos</p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

        <%-- Autorregistro del cliente (RF-13). Documento y correo no se pueden repetir (RF-14). --%>
        <form method="post" action="${pageContext.request.contextPath}/registro" autocomplete="on">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="rejilla-campos">
                <div class="grupo-campo">
                    <label for="nombres">Nombres</label>
                    <input type="text" id="nombres" name="nombres" maxlength="80" required autocomplete="given-name"
                           value="<c:out value='${cliente.nombres}' />">
                </div>
                <div class="grupo-campo">
                    <label for="apellidos">Apellidos</label>
                    <input type="text" id="apellidos" name="apellidos" maxlength="80" required autocomplete="family-name"
                           value="<c:out value='${cliente.apellidos}' />">
                </div>
                <div class="grupo-campo">
                    <label for="documento">Documento (DUI o pasaporte)</label>
                    <input type="text" id="documento" name="documento" maxlength="20" required placeholder="00000000-0"
                           value="<c:out value='${cliente.documento}' />">
                </div>
                <div class="grupo-campo">
                    <label for="telefono">Teléfono (opcional)</label>
                    <input type="tel" id="telefono" name="telefono" maxlength="20" autocomplete="tel" placeholder="7000-0000"
                           value="<c:out value='${cliente.telefono}' />">
                </div>
                <div class="grupo-campo completo">
                    <label for="correo">Correo electrónico</label>
                    <input type="email" id="correo" name="correo" maxlength="120" required autocomplete="email"
                           value="<c:out value='${cliente.correo}' />">
                </div>
                <div class="grupo-campo completo">
                    <label for="usuario">Usuario</label>
                    <input type="text" id="usuario" name="usuario" maxlength="30" required autocomplete="username"
                           placeholder="minúsculas, números, punto o guion" value="<c:out value='${usuario}' />">
                </div>
                <div class="grupo-campo">
                    <label for="clave">Contraseña</label>
                    <input type="password" id="clave" name="clave" required minlength="8" autocomplete="new-password">
                    <span class="ayuda">Mínimo 8 caracteres, con letras y números.</span>
                </div>
                <div class="grupo-campo">
                    <label for="confirmacion">Confirmar contraseña</label>
                    <input type="password" id="confirmacion" name="confirmacion" required minlength="8" autocomplete="new-password">
                </div>
            </div>
            <button type="submit" class="boton acento" style="width:100%">Crear cuenta</button>
        </form>
        <p class="ayuda" style="text-align:center;margin-top:16px">
            ¿Ya tiene cuenta? <a href="${pageContext.request.contextPath}/acceso">Iniciar sesión</a> ·
            <a href="${pageContext.request.contextPath}/">Volver al inicio</a>
        </p>
    </section>
</main>
</body>
</html>
