<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${empty recinto.id ? 'Nuevo recinto' : 'Editar recinto'}" />
<c:set var="paginaActiva" value="recintos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/organizador/recintos">Recintos</a> › <c:out value="${titulo}" /></div>
    <h1><c:out value="${titulo}" /></h1>
    <p class="subtitulo">Después de guardarlo podrá agregar sus zonas y generar los asientos.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta formulario">
        <form method="post" action="${ctx}/organizador/recintos">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${recinto.id}">
            <div class="rejilla-campos">
                <div class="grupo-campo completo">
                    <label for="nombre">Nombre</label>
                    <input type="text" id="nombre" name="nombre" maxlength="120" required
                           value="<c:out value='${recinto.nombre}' />">
                </div>
                <div class="grupo-campo">
                    <label for="tipo">Tipo</label>
                    <select id="tipo" name="tipo" required>
                        <c:forEach var="t" items="${tipos}">
                            <option value="${t}" ${recinto.tipo == t ? 'selected' : ''}>${t}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="direccion">Dirección</label>
                    <input type="text" id="direccion" name="direccion" maxlength="200"
                           value="<c:out value='${recinto.direccion}' />">
                </div>
                <c:if test="${not empty recinto.id}">
                    <div class="grupo-campo completo">
                        <label class="casilla">
                            <input type="checkbox" name="activo" value="si" ${recinto.activo ? 'checked' : ''}>
                            Recinto activo (disponible para nuevos eventos)
                        </label>
                    </div>
                </c:if>
            </div>
            <div class="acciones-formulario">
                <button type="submit" class="boton verde">Guardar</button>
                <a class="boton gris" href="${ctx}/organizador/recintos">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
