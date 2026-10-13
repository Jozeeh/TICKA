<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Categorías" />
<c:set var="paginaActiva" value="categorias" />
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
            <h1>Categorías</h1>
            <p class="subtitulo">Clasifican los eventos en la cartelera (Rock, Ópera, Fútbol...).</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta">
        <h2 style="margin-top:0">${empty categoria.id ? 'Nueva categoría' : 'Editar categoría'}</h2>
        <form method="post" action="${ctx}/organizador/categorias" class="filtros">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${categoria.id}">
            <div class="grupo-campo" style="min-width:280px">
                <label for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre" maxlength="60" required
                       value="<c:out value='${categoria.nombre}' />">
            </div>
            <button type="submit" class="boton verde">Guardar</button>
            <c:if test="${not empty categoria.id}">
                <a class="boton gris" href="${ctx}/organizador/categorias">Cancelar</a>
            </c:if>
        </form>
    </section>

    <section class="tarjeta tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr><th>Nombre</th><th class="numero">Eventos</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <c:forEach var="cat" items="${categorias}">
                <tr>
                    <td><c:out value="${cat.nombre}" /></td>
                    <td class="numero">${cat.totalEventos}</td>
                    <td class="acciones-linea">
                        <a class="boton amarillo chico" href="${ctx}/organizador/categorias?accion=editar&id=${cat.id}">Editar</a>
                        <c:if test="${cat.totalEventos == 0}">
                            <form method="post" action="${ctx}/organizador/categorias" class="linea"
                                  onsubmit="return confirm('¿Eliminar esta categoría?');">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="eliminar">
                                <input type="hidden" name="id" value="${cat.id}">
                                <button type="submit" class="boton rojo chico">Eliminar</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty categorias}">
                <tr><td colspan="3" class="vacio">Todavía no hay categorías.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
