<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Comprar boletos" />
<c:set var="paginaActiva" value="cartelera" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/#cartelera">Cartelera</a> ›
        <a href="${ctx}/evento?id=${evento.id}"><c:out value="${evento.nombre}" /></a> › Comprar</div>
    <div class="cabecera-pagina">
        <div>
            <h1><c:out value="${evento.nombre}" /></h1>
            <p class="subtitulo"><c:out value="${evento.recinto}" /> · ${evento.fechaLarga} · ${evento.horarioTexto}</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <c:if test="${not aLaVenta}">
        <div class="alerta aviso"><c:out value="${motivoSinVenta}" /></div>
    </c:if>

    <%-- Reserva vigente: continuar al pago con tarjeta (RF-17, RF-21) --%>
    <c:if test="${not empty reserva and aLaVenta}">
        <section class="tarjeta resumen-compra">
            <div class="cabecera-pagina" style="margin-bottom:8px">
                <h2 style="margin:0">Su reserva</h2>
                <span class="cuenta-regresiva" id="cuentaRegresiva" data-segundos="${segundosRestantes}">--:--</span>
            </div>
            <p class="ayuda">Los asientos quedan reservados para usted ${minutos} minutos. Si el tiempo termina, se liberan.</p>
            <table class="tabla">
                <thead><tr><th>Asiento</th><th class="numero">Precio</th></tr></thead>
                <tbody>
                <c:forEach var="a" items="${reserva}">
                    <tr><td><c:out value="${a.etiqueta}" /></td><td class="numero">${a.precioTexto}</td></tr>
                </c:forEach>
                </tbody>
                <tfoot><tr><th>Total</th><th class="numero">${totalReserva}</th></tr></tfoot>
            </table>
            <div class="acciones-linea" style="margin-top:14px">
                <a class="boton verde" href="${ctx}/cliente/pago?evento=${evento.id}">Continuar al pago</a>
                <form method="post" action="${ctx}/cliente/comprar" class="linea">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="evento" value="${evento.id}">
                    <input type="hidden" name="accion" value="cancelar">
                    <button type="submit" class="boton gris">Cancelar reserva</button>
                </form>
            </div>
        </section>
    </c:if>

    <%-- Paso 1: elegir asientos en el mapa (RF-16, RF-19) --%>
    <section class="tarjeta">
        <h2 style="margin-top:0">${empty reserva ? 'Elija sus asientos' : 'Cambiar la selección'}</h2>
        <p class="ayuda">Máximo ${maximo} asientos por compra. Disponibles ahora: ${conteo['Disponible']}.</p>
        <c:choose>
            <c:when test="${aLaVenta}">
                <form method="post" action="${ctx}/cliente/comprar">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="evento" value="${evento.id}">
                    <input type="hidden" name="accion" value="reservar">
                    <c:set var="textoBotonSeleccion" value="${empty reserva ? 'Reservar asientos' : 'Actualizar reserva'}" />
                    <%@ include file="/WEB-INF/vistas/plantilla/mapa-seleccion.jspf" %>
                </form>
            </c:when>
            <c:otherwise>
                <p class="vacio">La venta no está disponible para este evento.</p>
            </c:otherwise>
        </c:choose>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
<script>
    // Cuenta regresiva de la reserva (RF-18); al terminar recarga para mostrar los asientos liberados.
    (function () {
        var el = document.getElementById('cuentaRegresiva');
        if (!el) { return; }
        var restante = parseInt(el.dataset.segundos, 10);
        function pintar() {
            var m = Math.floor(restante / 60), s = restante % 60;
            el.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
            el.classList.toggle('urgente', restante <= 60);
            if (restante <= 0) { location.reload(); return; }
            restante--;
            setTimeout(pintar, 1000);
        }
        pintar();
    })();
</script>
</body>
</html>
