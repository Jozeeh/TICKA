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
<main class="container-xl py-4">
    <h1 class="h3 mb-1">Venta en mostrador</h1>
    <p class="text-body-secondary mb-4">Venda boletos a nombre de un cliente registrado (canal Mostrador). El pago solo se registra.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Paso 1: evento y cliente --%>
    <form method="get" action="${ctx}/admin/mostrador" class="card card-body shadow-sm mb-4 filtros d-flex flex-row flex-wrap gap-3 align-items-end">
        <div class="campo-filtro" style="min-width:300px">
            <label class="form-label" for="evento">Evento</label>
            <select class="form-select" id="evento" name="evento" required>
                <option value="">Seleccione...</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${idEvento == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /> (${ev.fechaTexto})</option>
                </c:forEach>
            </select>
        </div>
        <div class="campo-filtro">
            <label class="form-label" for="documento">Documento del cliente</label>
            <input class="form-control" type="text" id="documento" name="documento" required placeholder="00000000-0" value="<c:out value='${documento}' />">
        </div>
        <button type="submit" class="btn btn-primary">Continuar</button>
    </form>

    <c:if test="${not empty cliente}">
        <div class="alert alert-success">Cliente: <strong><c:out value="${cliente.nombreCompleto}" /></strong> ·
            <c:out value="${cliente.documento}" /> · <c:out value="${cliente.correo}" /></div>
    </c:if>

    <%-- Paso 2: asientos y forma de pago --%>
    <c:if test="${not empty evento and not empty cliente}">
        <section class="card card-body shadow-sm mb-4">
            <h2 class="h5 mb-3"><c:out value="${evento.nombre}" /></h2>
            <form method="post" action="${ctx}/admin/mostrador"
                  data-confirmar="¿Registrar la venta en mostrador?" data-boton="Sí, vender">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="evento" value="${evento.id}">
                <input type="hidden" name="cliente" value="${cliente.id}">
                <div class="mb-3" style="max-width:260px">
                    <label class="form-label" for="formaPago">Forma de pago</label>
                    <select class="form-select" id="formaPago" name="formaPago" required>
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
