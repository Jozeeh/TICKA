<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Elegir asientos" />
<c:set var="paginaActiva" value="cartelera" />
<%-- Pantalla de compra a pantalla completa (sin menú lateral): evento | mapa | zonas y selección. --%>
<c:set var="sinMenuLateral" value="${true}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body class="cuerpo-compra">
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="compra-app">

    <%-- ============ Izquierda: datos del evento y zonas (minimalista) ============ --%>
    <aside class="compra-col compra-info">
        <a class="ayuda" href="${ctx}/cliente/cartelera">← Volver a la cartelera</a>
        <div class="compra-evento">
            <h1><c:out value="${evento.nombre}" /></h1>
            <p><c:out value="${evento.participantes}" /></p>
            <dl class="dato-evento">
                <dt>Fecha</dt><dd>${evento.fechaLarga}</dd>
                <dt>Horario</dt><dd>${evento.horarioTexto}</dd>
                <dt>Lugar</dt><dd><c:out value="${evento.recinto}" /></dd>
            </dl>
        </div>
        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>
        <c:if test="${not aLaVenta}">
            <div class="alerta aviso"><c:out value="${motivoSinVenta}" /></div>
        </c:if>

        <section class="bloque-compra">
            <h2>Zonas y precios</h2>
            <ul class="lista-precios">
                <c:forEach var="z" items="${zonas}" varStatus="s">
                    <li>
                        <span>
                            <c:out value="${z.nombre}" />
                            <span class="ayuda">${z.disponibles} disponibles</span>
                        </span>
                        <span class="precio-zona">${empty z.precio ? '—' : z.precioTexto}</span>
                    </li>
                </c:forEach>
            </ul>
        </section>

    </aside>

    <%-- ============ Centro: mapa fijo con los asientos de la zona elegida ============ --%>
    <section class="compra-col compra-mapa">
        <span class="etiqueta-mapa" id="etiquetaMapa">Mapa referencial · elija una zona</span>
        <form method="post" action="${ctx}/cliente/comprar" id="formReserva" class="mapa-lienzo">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="evento" value="${evento.id}">
            <input type="hidden" name="accion" value="reservar">
            <div class="mapa-contenido" id="mapaContenido">
                <div class="mapa-zonas">
                    <div class="escenario">ESCENARIO / CANCHA</div>
                    <p class="mapa-vacio" id="mapaVacio">Elija una zona a la derecha para ver sus asientos.</p>
                    <c:forEach var="z" items="${zonas}" varStatus="s">
                        <div class="zona-bloque" data-zona="${z.idZona}">
                            <button type="button" class="zona-bloque-titulo" data-zona="${z.idZona}">
                                <c:out value="${z.nombre}" /> · ${empty z.precio ? 'sin precio' : z.precioTexto}
                            </button>
                            <c:forEach var="fila" items="${z.filas}">
                                <div class="fila-asientos">
                                    <span class="letra">${fila.letra}</span>
                                    <c:forEach var="a" items="${fila.asientos}">
                                        <c:choose>
                                            <c:when test="${aLaVenta and (a.estado == 'Disponible' or a.estado == 'Seleccionado')}">
                                                <label class="asiento-sel" title="<c:out value='${z.nombre}' /> fila ${fila.letra}, asiento ${a.numero}">
                                                    <input type="checkbox" name="asiento" value="${a.idEventoAsiento}"
                                                           data-zona="${z.idZona}" data-precio="${z.precio}"
                                                           data-etiqueta="<c:out value='${z.nombre}' /> ${a.etiqueta}"
                                                           ${a.estado == 'Seleccionado' ? 'checked' : ''}>
                                                    <span class="asiento Disponible">${a.numero}</span>
                                                </label>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="asiento ${a.estado == 'Seleccionado' ? 'Reservado' : a.estado}"
                                                      title="${a.etiqueta} · no disponible">${a.numero}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                            </c:forEach>
                        </div>
                    </c:forEach>
                </div>
                <c:if test="${empty zonas}">
                    <div class="alerta info">El mapa de asientos todavía no está disponible.</div>
                </c:if>
            </div>
        </form>
    </section>

    <%-- ============ Derecha: zonas (arriba) y selección / reserva (abajo) ============ --%>
    <aside class="compra-col compra-zonas dividida" id="panelZonas">
        <div class="panel-mitad panel-sectores">
            <h2>Seleccionar zona</h2>
            <ul class="lista-sectores">
                <c:forEach var="z" items="${zonas}" varStatus="s">
                    <li>
                        <button type="button" class="sector" data-zona="${z.idZona}" data-nombre="<c:out value='${z.nombre}' />">
                            <span>
                                <strong><c:out value="${z.nombre}" /></strong>
                                <span class="barra" title="${z.disponibles} de ${z.total} disponibles"><span style="width:${z.total == 0 ? 0 : z.disponibles * 100 / z.total}%"></span></span>
                            </span>
                            <span class="chevron" aria-hidden="true">›</span>
                        </button>
                    </li>
                </c:forEach>
            </ul>
            <div class="leyenda compacta">
                <span><i class="Disponible"></i> Disponible</span>
                <span><i class="Seleccionado"></i> Su selección</span>
                <span><i class="Reservado"></i> Reservado</span>
                <span><i class="Vendido"></i> Vendido</span>
                <span><i class="Bloqueado"></i> No disponible</span>
            </div>
        </div>

        <div class="panel-mitad panel-seleccion">
            <%-- Reserva vigente (RF-18): continuar al pago o cancelar --%>
            <c:if test="${not empty reserva and aLaVenta}">
                <section class="reserva-activa">
                    <div class="fila-entre">
                        <h2>Su reserva</h2>
                        <span class="cuenta-regresiva" id="cuentaRegresiva" data-segundos="${segundosRestantes}">--:--</span>
                    </div>
                    <ul class="resumen-lineas">
                        <c:forEach var="a" items="${reserva}">
                            <li><span><c:out value="${a.etiqueta}" /></span><span>${a.precioTexto}</span></li>
                        </c:forEach>
                        <li class="total"><span>Total</span><span>${totalReserva}</span></li>
                    </ul>
                    <div class="acciones-linea">
                        <a class="boton verde" href="${ctx}/cliente/pago?evento=${evento.id}">Continuar al pago</a>
                        <form method="post" action="${ctx}/cliente/comprar" class="linea">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="evento" value="${evento.id}">
                            <input type="hidden" name="accion" value="cancelar">
                            <button type="submit" class="boton gris">Cancelar</button>
                        </form>
                    </div>
                </section>
            </c:if>

            <%-- Selección actual (se arma con JavaScript) y botón para reservar --%>
            <c:if test="${aLaVenta}">
                <section>
                    <h2>Su selección</h2>
                    <ul class="resumen-lineas" id="listaSeleccion"><li class="vacio-sel">Elija una zona y marque sus asientos en el mapa.</li></ul>
                    <div class="fila-entre total-seleccion"><span>Total</span><strong id="totalSeleccion">$0.00</strong></div>
                    <p class="ayuda">Máximo ${maximo} asientos por compra. Al reservar, quedan apartados ${minutos} minutos.</p>
                    <button type="submit" form="formReserva" class="boton acento acento-grande" id="botonReservar" disabled>
                        ${empty reserva ? 'Reservar asientos' : 'Actualizar reserva'}</button>
                </section>
            </c:if>
        </div>
    </aside>
</main>

<script>
    (function () {
        var maximo = ${empty maximo ? 6 : maximo};
        var contenido = document.getElementById('mapaContenido');
        var lienzo = document.getElementById('formReserva');
        var etiquetaMapa = document.getElementById('etiquetaMapa');

        function casillas() { return Array.prototype.slice.call(document.querySelectorAll('input[name="asiento"]')); }
        function marcadas() { return casillas().filter(function (c) { return c.checked; }); }

        // Mapa fijo: se escala para que la zona elegida quepa en el centro (no se explora).
        function ajustar() {
            contenido.style.transform = 'none';
            var zonas = contenido.querySelector('.mapa-zonas');
            if (!zonas) { return; }
            var l = lienzo.getBoundingClientRect(), r = zonas.getBoundingClientRect();
            var e = Math.min((l.width - 32) / r.width, (l.height - 32) / r.height, 1.6);
            var dx = (l.width - r.width * e) / 2, dy = (l.height - r.height * e) / 2;
            contenido.style.transform = 'translate(' + dx + 'px,' + dy + 'px) scale(' + e + ')';
        }

        // Elegir zona: en el mapa solo se muestran los asientos de esa zona.
        // Las demás se ocultan, pero sus casillas marcadas siguen en el formulario.
        function elegirZona(id) {
            document.querySelectorAll('.zona-bloque').forEach(function (b) {
                b.classList.toggle('oculta', b.dataset.zona !== id);
            });
            document.getElementById('mapaVacio').hidden = true;
            document.querySelectorAll('.sector').forEach(function (s) { s.classList.toggle('activo', s.dataset.zona === id); });
            var sector = document.querySelector('.sector[data-zona="' + id + '"]');
            etiquetaMapa.textContent = sector ? sector.dataset.nombre : '';
            ajustar();
        }

        function avisarLimite() {
            if (window.avisar) { avisar('Límite de asientos', 'Puede elegir como máximo ' + maximo + ' asientos por compra.'); }
        }

        // "Su selección" (columna derecha): asientos marcados, total y botón de reservar.
        function actualizar() {
            var ul = document.getElementById('listaSeleccion');
            if (!ul) { return; }
            var sel = marcadas();
            var total = 0;
            ul.innerHTML = '';
            sel.forEach(function (c) {
                total += parseFloat(c.dataset.precio || 0);
                var li = document.createElement('li');
                var a = document.createElement('span');
                a.textContent = c.dataset.etiqueta;
                var b = document.createElement('span');
                b.textContent = '$' + parseFloat(c.dataset.precio || 0).toFixed(2);
                li.appendChild(a);
                li.appendChild(b);
                ul.appendChild(li);
            });
            if (!sel.length) {
                var v = document.createElement('li');
                v.className = 'vacio-sel';
                v.textContent = 'Elija una zona y marque sus asientos en el mapa.';
                ul.appendChild(v);
            }
            document.getElementById('totalSeleccion').textContent = '$' + total.toFixed(2);
            document.getElementById('botonReservar').disabled = sel.length === 0 || sel.length > maximo;
        }

        casillas().forEach(function (c) {
            c.addEventListener('change', function () {
                if (c.checked && marcadas().length > maximo) { c.checked = false; avisarLimite(); }
                elegirZona(c.dataset.zona);
                actualizar();
            });
        });
        document.querySelectorAll('.sector, .zona-bloque-titulo').forEach(function (b) {
            b.addEventListener('click', function () { elegirZona(b.dataset.zona); });
        });
        window.addEventListener('resize', ajustar);

        // Al entrar no se muestra ninguna zona, salvo la de una reserva vigente.
        document.querySelectorAll('.zona-bloque').forEach(function (b) { b.classList.add('oculta'); });
        actualizar();
        var primera = marcadas()[0];
        if (primera) { elegirZona(primera.dataset.zona); } else { ajustar(); }

        // Cuenta regresiva de la reserva (RF-18).
        var reloj = document.getElementById('cuentaRegresiva');
        if (reloj) {
            var restante = parseInt(reloj.dataset.segundos, 10);
            (function pintar() {
                var m = Math.floor(restante / 60), s = restante % 60;
                reloj.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
                reloj.classList.toggle('urgente', restante <= 60);
                if (restante <= 0) { location.reload(); return; }
                restante--;
                setTimeout(pintar, 1000);
            })();
        }
    })();
</script>
<%-- SweetAlert para el aviso del límite (esta página no incluye el pie común). --%>
<script src="${ctx}/resources/js/sweetalert2.all.min.js"></script>
<script src="${ctx}/resources/js/confirmaciones.js?v=1"></script>
</body>
</html>
