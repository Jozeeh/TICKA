<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Configuración inicial" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<main class="pagina-acceso">
    <section class="caja-acceso">
        <div class="marca-grande">TICK<span>A</span></div>
        <p class="lema">Configuración inicial</p>

        <div class="alerta info">La base de datos no tiene un administrador activo.
            Cree la primera cuenta de administrador para comenzar.</div>
        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

        <form method="post" action="${pageContext.request.contextPath}/configuracion-inicial">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <div class="grupo-campo">
                <label for="nombre">Nombre completo</label>
                <input type="text" id="nombre" name="nombre" maxlength="100" required
                       value="<c:out value='${nombre}' />">
            </div>
            <div class="grupo-campo">
                <label for="usuario">Usuario</label>
                <input type="text" id="usuario" name="usuario" maxlength="30" required
                       autocomplete="username" value="<c:out value='${usuario}' />">
                <span class="ayuda">De 4 a 30 caracteres: minúsculas, números, punto, guion o guion bajo.</span>
            </div>
            <div class="grupo-campo">
                <label for="clave">Contraseña</label>
                <input type="password" id="clave" name="clave" required autocomplete="new-password">
                <span class="ayuda">Mínimo 8 caracteres, con letras y números.</span>
            </div>
            <div class="grupo-campo">
                <label for="confirmacion">Confirmar contraseña</label>
                <input type="password" id="confirmacion" name="confirmacion" required autocomplete="new-password">
            </div>
            <button type="submit" class="boton acento">Crear administrador</button>
        </form>
    </section>
</main>
</body>
</html>
