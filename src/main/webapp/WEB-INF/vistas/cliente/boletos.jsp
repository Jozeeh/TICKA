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
<main class="contenedor">
    <div class="cabecera-pagina">
        <div>
            <h1>Mis boletos</h1>
            <p class="subtitulo">Sus compras con cada boleto y su código QR. Puede anular una compra hasta ${horasAnulacion} horas antes de la apertura de puertas.</p>
        </div>
        <a class="boton acento" href="${ctx}/#cartelera">Comprar más boletos</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <c:if test="${empty compras and empty error}">
        <div class="tarjeta vacio">Todavía no tiene compras. Explore la <a href="${ctx}/#cartelera">cartelera</a> y elija sus asientos.</div>
    </c:if>

    <c:forEach var="compra" items="${compras}">
        <section class="tarjeta compra-cliente" id="venta-${compra.id}">
            <div class="compra-cliente-cabecera">
                <div>
                    <h2 style="margin:0"><c:out value="${compra.evento}" /></h2>
                    <p class="subtitulo" style="margin:4px 0 0">${compra.fechaEventoTexto} · <c:out value="${compra.recinto}" /></p>
                    <p class="ayuda" style="margin:4px 0 0">Compra #${compra.id} · ${compra.fechaTexto} · <c:out value="${compra.canal}" /> · <c:out value="${compra.formaPago}" /></p>
                </div>
                <div style="text-align:right">
                    <span class="etiqueta ${compra.estado == 'Pagada' ? 'activo' : 'inactivo'}"><c:out value="${compra.estado}" /></span>
                    <div class="total-compra">${compra.totalTexto}</div>
                </div>
            </div>

            <c:if test="${not empty compra.motivoAnulacion}">
                <p class="ayuda">Motivo de anulación: <c:out value="${compra.motivoAnulacion}" /></p>
            </c:if>

            <div class="boletos">
                <c:forEach var="b" items="${compra.boletos}">
                    <div class="boleto ${b.estado}">
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
                            <span class="etiqueta ${b.estado == 'Emitida' ? 'activo' : b.estado == 'Usada' ? 'Finalizado' : 'inactivo'}"><c:out value="${b.estado}" /></span>
                            <code class="codigo-boleto"><c:out value="${b.codigo}" /></code>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div class="acciones-linea" style="margin-top:12px">
                <c:url var="urlComprobante" value="/cliente/boletos"><c:param name="comprobante" value="${compra.id}" /></c:url>
                <a class="boton borde chico" target="_blank" href="${urlComprobante}">Comprobante PDF</a>
                <c:choose>
                    <c:when test="${compra.anulable}">
                        <%-- Anulación con motivo (RF-23, RN-12) --%>
                        <form method="post" action="${ctx}/cliente/boletos" class="acciones-linea"
                              data-confirmar="¿Anular esta compra?" data-peligro="si" data-boton="Sí, anular"
                              data-detalle="Se anularán todos sus boletos y los asientos quedarán libres. El reembolso es simulado.">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="accion" value="anular">
                            <input type="hidden" name="venta" value="${compra.id}">
                            <input type="text" name="motivo" required minlength="5" maxlength="200"
                                   placeholder="Motivo de la anulación" style="width:240px">
                            <button type="submit" class="boton rojo chico">Anular compra</button>
                        </form>
                    </c:when>
                    <c:when test="${compra.estado == 'Pagada'}">
                        <span class="ayuda"><c:out value="${compra.motivoNoAnulable}" /></span>
                    </c:when>
                </c:choose>
            </div>
        </section>
    </c:forEach>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
