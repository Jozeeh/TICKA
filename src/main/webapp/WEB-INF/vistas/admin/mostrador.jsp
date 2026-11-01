<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Venta en mostrador" />
<c:set var="paginaActiva" value="mostrador" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <h1>Venta en mostrador</h1>
    <p class="subtitulo">Venda boletos a nombre de un cliente registrado (canal Mostrador). El pago solo se registra.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Paso 1: evento y cliente --%>
    <form method="get" action="${ctx}/admin/mostrador" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:300px">
            <label for="evento">Evento</label>
            <select id="evento" name="evento" required>
                <option value="">Seleccione...</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${idEvento == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /> (${ev.fechaTexto})</option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="documento">Documento del cliente</label>
            <input type="text" id="documento" name="documento" required placeholder="00000000-0" value="<c:out value='${documento}' />">
        </div>
        <button type="submit" class="boton">Continuar</button>
    </form>

    <c:if test="${not empty cliente}">
        <div class="alerta exito">Cliente: <strong><c:out value="${cliente.nombreCompleto}" /></strong> ·
            <c:out value="${cliente.documento}" /> · <c:out value="${cliente.correo}" /></div>
    </c:if>

    <%-- Paso 2: asientos y forma de pago --%>
    <c:if test="${not empty evento and not empty cliente}">
        <section class="tarjeta">
            <h2 style="margin-top:0"><c:out value="${evento.nombre}" /></h2>
            <form method="post" action="${ctx}/admin/mostrador"
                  data-confirmar="¿Registrar la venta en mostrador?" data-boton="Sí, vender">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="evento" value="${evento.id}">
                <input type="hidden" name="cliente" value="${cliente.id}">
                <div class="grupo-campo" style="max-width:260px">
                    <label for="formaPago">Forma de pago</label>
                    <select id="formaPago" name="formaPago" required>
                        <c:forEach var="f" items="${formasPago}"><option value="${f}">${f}</option></c:forEach>
                    </select>
                </div>
                <c:set var="textoBotonSeleccion" value="Registrar venta" />
                <%@ include file="/WEB-INF/vistas/plantilla/mapa-seleccion.jspf" %>
            </form>
        </section>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
