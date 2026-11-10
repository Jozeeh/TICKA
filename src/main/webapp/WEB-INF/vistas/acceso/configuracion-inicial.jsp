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
        <h1 class="h3 mb-1">Configuración inicial</h1>
        <p class="acceso-sub">La base de datos no tiene un administrador activo. Cree la primera cuenta de administrador para comenzar.</p>

        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

        <form method="post" action="${pageContext.request.contextPath}/configuracion-inicial">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <label for="nombre" class="visually-hidden">Nombre completo</label>
            <input class="form-control" type="text" id="nombre" name="nombre" maxlength="100" required placeholder="Nombre completo"
                   value="<c:out value='${nombre}' />">

            <label for="usuario" class="visually-hidden">Usuario</label>
            <input class="form-control" type="text" id="usuario" name="usuario" maxlength="30" required autocomplete="username"
                   placeholder="Usuario (4 a 30: minúsculas, números, punto o guion)" value="<c:out value='${usuario}' />">

            <div class="row gx-3">
                <div class="col-sm-6">
                    <label for="clave" class="visually-hidden">Contraseña</label>
                    <div class="campo-clave">
                        <input class="form-control" type="password" id="clave" name="clave" required autocomplete="new-password" placeholder="Contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
                <div class="col-sm-6">
                    <label for="confirmacion" class="visually-hidden">Confirmar contraseña</label>
                    <div class="campo-clave">
                        <input class="form-control" type="password" id="confirmacion" name="confirmacion" required autocomplete="new-password" placeholder="Confirmar contraseña">
                        <button type="button" aria-label="Mostrar contraseña"><%@ include file="/WEB-INF/vistas/plantilla/icono-ojo.jspf" %></button>
                    </div>
                </div>
            </div>
            <p class="acceso-nota">Mínimo 8 caracteres, con letras y números.</p>
            <button type="submit" class="btn btn-acento btn-lg w-100">Crear administrador</button>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/clave-visible.jspf" %>
</body>
</html>
