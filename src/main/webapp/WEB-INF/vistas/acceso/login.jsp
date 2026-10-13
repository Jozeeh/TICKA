<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Iniciar sesión" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<main class="pagina-acceso">
    <section class="caja-acceso">
        <div class="marca-grande">TICK<span>A</span></div>
        <p class="lema">Sistema de Eventos en Vivo</p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>
        <c:if test="${not empty mensajeExito}">
            <div class="alerta exito"><c:out value="${mensajeExito}" /></div>
        </c:if>
        <c:if test="${not empty mensajeAviso}">
            <div class="alerta aviso"><c:out value="${mensajeAviso}" /></div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/acceso" autocomplete="on">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="grupo-campo">
                <label for="usuario">Usuario</label>
                <input type="text" id="usuario" name="usuario" maxlength="50" required
                       autocomplete="username" value="<c:out value='${ultimoUsuario}' />">
            </div>
            <div class="grupo-campo">
                <label for="clave">Contraseña</label>
                <input type="password" id="clave" name="clave" required autocomplete="current-password">
            </div>
            <div class="grupo-campo">
                <label class="casilla">
                    <input type="checkbox" name="recordar" value="si" ${not empty ultimoUsuario ? 'checked' : ''}>
                    Recordar mi usuario en este equipo
                </label>
            </div>
            <button type="submit" class="boton acento">Ingresar</button>
        </form>
        <p class="ayuda" style="text-align:center;margin-top:16px">
            <a href="${pageContext.request.contextPath}/">Volver al inicio</a>
        </p>
    </section>
</main>
</body>
</html>
