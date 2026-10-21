<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Recintos" />
<c:set var="paginaActiva" value="recintos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="cabecera-pagina">
        <div>
            <h1>Recintos</h1>
            <p class="subtitulo">Lugares físicos con sus zonas y asientos numerados.</p>
        </div>
        <a class="boton verde" href="${ctx}/organizador/recintos?accion=nuevo">Nuevo recinto</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr>
                <th>Nombre</th><th>Tipo</th><th>Dirección</th>
                <th class="numero">Zonas</th><th class="numero">Asientos activos</th>
                <th>Estado</th><th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="r" items="${recintos}">
                <tr>
                    <td><strong><c:out value="${r.nombre}" /></strong></td>
                    <td><c:out value="${r.tipo}" /></td>
                    <td><c:out value="${r.direccion}" /></td>
                    <td class="numero">${r.totalZonas}</td>
                    <td class="numero">${r.totalAsientos}</td>
                    <td><span class="etiqueta ${r.activo ? 'activo' : 'inactivo'}">${r.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                        <div class="acciones-linea">
                            <a class="boton chico" href="${ctx}/organizador/zonas?recinto=${r.id}">Zonas y asientos</a>
                            <a class="boton amarillo chico" href="${ctx}/organizador/recintos?accion=editar&id=${r.id}">Editar</a>
                            <form method="post" action="${ctx}/organizador/recintos" class="linea">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="estado">
                                <input type="hidden" name="id" value="${r.id}">
                                <button type="submit" class="boton chico ${r.activo ? 'rojo' : 'verde'}">${r.activo ? 'Desactivar' : 'Activar'}</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty recintos}">
                <tr><td colspan="7" class="vacio">Todavía no hay recintos registrados.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
