<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Clientes" />
<c:set var="paginaActiva" value="clientes" />
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
            <h1>Clientes</h1>
            <p class="subtitulo">Cuentas creadas por autorregistro en el portal. Aquí solo se consultan y se activan o desactivan.</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <form method="get" action="${ctx}/admin/clientes" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:320px">
            <label for="texto">Buscar</label>
            <input type="text" id="texto" name="texto" placeholder="Nombre, documento, correo o usuario"
                   value="<c:out value='${texto}' />">
        </div>
        <button type="submit" class="boton">Buscar</button>
        <a class="boton gris" href="${ctx}/admin/clientes">Limpiar</a>
    </form>

    <section class="tarjeta tabla-contenedor">
        <table class="tabla">
            <thead>
            <tr>
                <th>Cliente</th><th>Usuario</th><th>Documento</th><th>Correo</th><th>Teléfono</th>
                <th class="numero">Compras</th><th class="numero">Boletos</th><th class="numero">Total</th>
                <th>Estado</th><th>Acción</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="cl" items="${clientes}">
                <tr>
                    <td><strong><c:out value="${cl.nombreCompleto}" /></strong></td>
                    <td><c:out value="${cl.username}" /></td>
                    <td><c:out value="${cl.documento}" /></td>
                    <td><c:out value="${cl.correo}" /></td>
                    <td><c:out value="${cl.telefono}" /></td>
                    <td class="numero">${cl.compras}</td>
                    <td class="numero">${cl.boletos}</td>
                    <td class="numero">${cl.totalGastadoTexto}</td>
                    <td><span class="etiqueta ${cl.activo ? 'activo' : 'inactivo'}">${cl.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td>
                        <form method="post" action="${ctx}/admin/clientes" class="linea">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="usuario" value="${cl.idUsuario}">
                            <input type="hidden" name="activar" value="${cl.activo ? 'no' : 'si'}">
                            <button type="submit" class="boton chico ${cl.activo ? 'rojo' : 'verde'}">${cl.activo ? 'Desactivar' : 'Activar'}</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty clientes}">
                <tr><td colspan="10" class="vacio">No se encontraron clientes.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
