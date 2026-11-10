<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${cliente.nombreCompleto}" />
<c:set var="paginaActiva" value="clientes" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/admin/clientes">Clientes</a></li><li class="breadcrumb-item active" aria-current="page"><c:out value="${cliente.nombreCompleto}" /></li></ol></nav>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1"><c:out value="${cliente.nombreCompleto}" />
                <span class="badge rounded-pill etiqueta ${cliente.activo ? 'activo' : 'inactivo'}">${cliente.activo ? 'Activo' : 'Inactivo'}</span></h1>
            <p class="text-body-secondary mb-0">Detalle del cliente con todas sus compras y boletos (solo consulta).</p>
        </div>
        <form method="post" action="${ctx}/admin/clientes" class="d-inline"
              data-confirmar="${cliente.activo ? '¿Desactivar la cuenta del cliente?' : '¿Activar la cuenta del cliente?'}"
              data-detalle="${cliente.activo ? 'Ya no podrá iniciar sesión en el portal.' : ''}"
              data-boton="${cliente.activo ? 'Sí, desactivar' : 'Sí, activar'}" data-peligro="${cliente.activo ? 'si' : 'no'}">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="usuario" value="${cliente.idUsuario}">
            <input type="hidden" name="cliente" value="${cliente.id}">
            <input type="hidden" name="activar" value="${cliente.activo ? 'no' : 'si'}">
            <button type="submit" class="btn ${cliente.activo ? 'btn-danger' : 'btn-success'}">${cliente.activo ? 'Desactivar cuenta' : 'Activar cuenta'}</button>
        </form>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="row row-cols-2 row-cols-md-3 row-cols-xl-4 g-3 mb-4 indicadores compactos">
        <div class="col"><div class="card h-100 indicador"><div class="card-body"><div class="valor">${cliente.compras}</div><div class="nombre">Compras pagadas</div></div></div></div>
        <div class="col"><div class="card h-100 indicador acento"><div class="card-body"><div class="valor">${cliente.boletos}</div><div class="nombre">Boletos vigentes</div></div></div></div>
        <div class="col"><div class="card h-100 indicador verde"><div class="card-body"><div class="valor">${cliente.totalGastadoTexto}</div><div class="nombre">Total gastado</div></div></div></div>
    </section>

    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">Datos del cliente</h2>
        <dl class="row mb-0">
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Usuario</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${cliente.username}" /></dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Documento</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${cliente.documento}" /></dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Correo</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${cliente.correo}" /></dd>
            <dt class="col-sm-4 col-lg-3 text-body-secondary">Teléfono</dt><dd class="col-sm-8 col-lg-9 mb-1"><c:out value="${cliente.telefono}" default="—" /></dd>
        </dl>
    </section>

    <section class="card card-body shadow-sm mb-4">
        <h2 class="h5 mb-3">Compras</h2>
        <c:if test="${empty compras}">
            <p class="text-center text-body-secondary py-4">Este cliente todavía no tiene compras.</p>
        </c:if>
        <c:forEach var="compra" items="${compras}">
            <div class="card mb-3 overflow-hidden">
                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <div>
                        <strong>Venta #${compra.id}</strong>
                        <span class="small text-body-secondary"> · ${compra.fechaTexto} · <c:out value="${compra.canal}" /> · <c:out value="${compra.formaPago}" /></span>
                    </div>
                    <div>
                        <span class="badge rounded-pill etiqueta ${compra.estado == 'Pagada' ? 'activo' : 'inactivo'}"><c:out value="${compra.estado}" /></span>
                        <strong class="ms-2">${compra.totalTexto}</strong>
                    </div>
                </div>
                <c:if test="${not empty compra.motivoAnulacion}">
                    <p class="small text-body-secondary mx-3 my-2">Motivo de anulación: <c:out value="${compra.motivoAnulacion}" /></p>
                </c:if>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead>
                        <tr><th>Evento</th><th>Fecha del evento</th><th>Zona</th><th>Asiento</th>
                            <th class="text-end">Precio</th><th>Estado</th><th>Código</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="b" items="${compra.boletos}">
                            <tr>
                                <td><c:out value="${b.evento}" /></td>
                                <td>${b.fechaEventoTexto}</td>
                                <td><c:out value="${b.zona}" /></td>
                                <td><c:out value="${b.asiento}" /></td>
                                <td class="text-end">${b.precioTexto}</td>
                                <td><c:out value="${b.estado}" /></td>
                                <td><code><c:out value="${b.codigoCorto}" /></code></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:forEach>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
