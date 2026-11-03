<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Iniciar sesión" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body class="cuerpo-acceso">
<main class="acceso-pantalla">
    <%@ include file="/WEB-INF/vistas/plantilla/acceso-imagen.jspf" %>

    <section class="acceso-formulario">
        <h1>Iniciar sesión</h1>
        <p class="acceso-sub">¿No tienes cuenta? <a href="${pageContext.request.contextPath}/registro">Crear cuenta</a></p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>
        <c:if test="${not empty mensajeExito}">
            <div class="alerta exito"><c:out value="${mensajeExito}" /></div>
        </c:if>
        <c:if test="${not empty mensajeAviso}">
            <div class="alerta aviso"><c:out value="${mensajeAviso}" /></div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/acceso" autocomplete="on">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <label for="usuario" class="solo-lector">Usuario</label>
            <input type="text" id="usuario" name="usuario" maxlength="50" required placeholder="Usuario"
                   autocomplete="username" value="<c:out value='${ultimoUsuario}' />" ${empty ultimoUsuario ? 'autofocus' : ''}>

            <label for="clave" class="solo-lector">Contraseña</label>
            <div class="campo-clave">
                <input type="password" id="clave" name="clave" required placeholder="Contraseña"
                       autocomplete="current-password" ${not empty ultimoUsuario ? 'autofocus' : ''}>
                <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
            </div>

            <label class="casilla acceso-casilla">
                <input type="checkbox" name="recordar" value="si" ${not empty ultimoUsuario ? 'checked' : ''}>
                Recordar mi usuario en este equipo
            </label>
            <button type="submit" class="boton acento acento-grande">Ingresar</button>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/clave-visible.jspf" %>
</body>
</html>
