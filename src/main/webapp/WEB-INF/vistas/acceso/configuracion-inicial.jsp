<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Configuración inicial" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body class="cuerpo-acceso">
<main class="acceso-pantalla">
    <%@ include file="/WEB-INF/vistas/plantilla/acceso-imagen.jspf" %>

    <section class="acceso-formulario">
        <h1>Configuración inicial</h1>
        <p class="acceso-sub">La base de datos no tiene un administrador activo. Cree la primera cuenta de administrador para comenzar.</p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

        <form method="post" action="${pageContext.request.contextPath}/configuracion-inicial">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <label for="nombre" class="solo-lector">Nombre completo</label>
            <input type="text" id="nombre" name="nombre" maxlength="100" required placeholder="Nombre completo"
                   value="<c:out value='${nombre}' />">

            <label for="usuario" class="solo-lector">Usuario</label>
            <input type="text" id="usuario" name="usuario" maxlength="30" required autocomplete="username"
                   placeholder="Usuario (4 a 30: minúsculas, números, punto o guion)" value="<c:out value='${usuario}' />">

            <div class="acceso-par">
                <div>
                    <label for="clave" class="solo-lector">Contraseña</label>
                    <div class="campo-clave">
                        <input type="password" id="clave" name="clave" required autocomplete="new-password" placeholder="Contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
                <div>
                    <label for="confirmacion" class="solo-lector">Confirmar contraseña</label>
                    <div class="campo-clave">
                        <input type="password" id="confirmacion" name="confirmacion" required autocomplete="new-password" placeholder="Confirmar contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
            </div>
            <p class="acceso-nota">Mínimo 8 caracteres, con letras y números.</p>
            <button type="submit" class="boton acento acento-grande">Crear administrador</button>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/clave-visible.jspf" %>
</body>
</html>
