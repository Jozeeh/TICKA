<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Pago aprobado" />
<c:set var="paginaActiva" value="pago" />
<c:set var="sinMenuLateral" value="${true}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor checkout">
    <ol class="pasos-compra">
        <li class="hecho"><span>✓</span> Asientos</li>
        <li class="hecho"><span>✓</span> Pago</li>
        <li class="actual"><span>3</span> Confirmación</li>
    </ol>

    <section class="tarjeta pago-aprobado">
        <div class="icono-aprobado" aria-hidden="true">✓</div>
        <h1>¡Pago aprobado!</h1>
        <p class="subtitulo">Gracias, <c:out value="${cliente.nombres}" />. Sus boletos con código QR ya están listos.</p>

        <ul class="resumen-lineas">
            <li><span>Orden</span><span>#${compra.id}</span></li>
            <li><span>Evento</span><span><c:out value="${compra.evento}" /></span></li>
            <li><span>Fecha</span><span>${compra.fechaEventoTexto}</span></li>
            <li><span>Boletos</span><span>${compra.boletos.size()}</span></li>
            <li><span>Pagado con</span><span>Tarjeta</span></li>
            <li class="total"><span>Total</span><span>${compra.totalTexto}</span></li>
        </ul>

        <div class="acciones-linea" style="justify-content:center;margin-top:18px">
            <a class="boton acento" href="${ctx}/cliente/boletos#venta-${compra.id}">Ver mis boletos</a>
            <c:url var="urlComprobante" value="/cliente/boletos"><c:param name="comprobante" value="${compra.id}" /></c:url>
            <a class="boton borde" target="_blank" href="${urlComprobante}">Comprobante PDF</a>
        </div>
        <p class="nota-segura">Pago simulado: no se realizó ningún cobro real.</p>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
