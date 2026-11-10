<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Mis boletos" />
<c:set var="paginaActiva" value="boletos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1">Mis boletos</h1>
            <p class="text-body-secondary mb-0">Sus compras con cada boleto y su código QR. Puede anular una compra hasta ${horasAnulacion} horas antes de la apertura de puertas.</p>
        </div>
        <a class="btn btn-acento" href="${ctx}/cliente/cartelera">Comprar más boletos</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <c:if test="${empty compras and empty error}">
        <div class="card card-body shadow-sm mb-4 text-center text-body-secondary py-4">Todavía no tiene compras. Explore la <a href="${ctx}/cliente/cartelera">cartelera</a> y elija sus asientos.</div>
    </c:if>

    <c:forEach var="compra" items="${compras}">
        <section class="card card-body shadow-sm mb-4 compra-cliente" id="venta-${compra.id}">
            <div class="d-flex flex-wrap justify-content-between gap-3 mb-3">
                <div>
                    <h2 class="h5 mb-0"><c:out value="${compra.evento}" /></h2>
                    <p class="text-body-secondary mb-0 mt-1">${compra.fechaEventoTexto} · <c:out value="${compra.recinto}" /></p>
                    <p class="small text-body-secondary mb-0 mt-1">Compra #${compra.id} · ${compra.fechaTexto} · <c:out value="${compra.canal}" /> · <c:out value="${compra.formaPago}" /></p>
                </div>
                <div class="text-end">
                    <span class="badge rounded-pill etiqueta ${compra.estado == 'Pagada' ? 'activo' : 'inactivo'}"><c:out value="${compra.estado}" /></span>
                    <div class="fs-4 fw-bold text-primary mt-1">${compra.totalTexto}</div>
                </div>
            </div>

            <c:if test="${not empty compra.motivoAnulacion}">
                <p class="small text-body-secondary">Motivo de anulación: <c:out value="${compra.motivoAnulacion}" /></p>
            </c:if>

            <div class="row row-cols-1 row-cols-md-2 row-cols-xxl-3 g-3">
                <c:forEach var="b" items="${compra.boletos}">
                    <div class="col"><div class="boleto ${b.estado} d-flex align-items-center gap-3 h-100 p-2 rounded-3">
                        <c:choose>
                            <c:when test="${b.estado != 'Anulada'}">
                                <c:url var="urlQr" value="/cliente/boletos"><c:param name="qr" value="${b.id}" /></c:url>
                                <img src="${urlQr}" alt="Código QR del boleto ${b.asiento}" width="120" height="120" loading="lazy">
                            </c:when>
                            <c:otherwise><div class="qr-anulado">ANULADO</div></c:otherwise>
                        </c:choose>
                        <div>
                            <strong><c:out value="${b.zona}" /> · ${b.asiento}</strong>
                            <div>${b.precioTexto}</div>
                            <span class="badge rounded-pill etiqueta ${b.estado == 'Emitida' ? 'activo' : b.estado == 'Usada' ? 'Finalizado' : 'inactivo'}"><c:out value="${b.estado}" /></span>
                            <code class="codigo-boleto"><c:out value="${b.codigo}" /></code>
                        </div>
                    </div></div>
                </c:forEach>
            </div>

            <div class="d-flex flex-wrap gap-2 align-items-center mt-3">
                <c:url var="urlComprobante" value="/cliente/boletos"><c:param name="comprobante" value="${compra.id}" /></c:url>
                <a class="btn btn-outline-primary btn-sm" target="_blank" href="${urlComprobante}">Comprobante PDF</a>
                <c:choose>
                    <c:when test="${compra.anulable}">
                        <%-- Anulación con motivo (RF-23, RN-12) --%>
                        <form method="post" action="${ctx}/cliente/boletos" class="d-flex flex-wrap gap-2 align-items-center"
                              data-confirmar="¿Anular esta compra?" data-peligro="si" data-boton="Sí, anular"
                              data-detalle="Se anularán todos sus boletos y los asientos quedarán libres. El reembolso es simulado.">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="accion" value="anular">
                            <input type="hidden" name="venta" value="${compra.id}">
                            <input class="form-control form-control-sm" type="text" name="motivo" required minlength="5" maxlength="200"
                                   placeholder="Motivo de la anulación" style="width:240px">
                            <button type="submit" class="btn btn-danger btn-sm">Anular compra</button>
                        </form>
                    </c:when>
                    <c:when test="${compra.estado == 'Pagada'}">
                        <span class="small text-body-secondary"><c:out value="${compra.motivoNoAnulable}" /></span>
                    </c:when>
                </c:choose>
            </div>
        </section>
    </c:forEach>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
