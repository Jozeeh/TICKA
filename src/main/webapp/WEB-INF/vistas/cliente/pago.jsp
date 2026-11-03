<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Pago" />
<%-- Pantalla de pago enfocada (sin menú lateral), como una pasarela de pago. --%>
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
        <li class="actual"><span>2</span> Pago</li>
        <li><span>3</span> Confirmación</li>
    </ol>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <div class="checkout-rejilla">
        <%-- Formulario: facturación y tarjeta (validación ficticia: solo que los datos estén escritos) --%>
        <form method="post" action="${ctx}/cliente/pago" id="formPago" class="checkout-formulario" autocomplete="on">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="evento" value="${evento.id}">

            <section class="tarjeta">
                <h2 class="checkout-titulo"><span>1</span> Datos de facturación</h2>
                <div class="rejilla-campos">
                    <div class="grupo-campo">
                        <label for="nombre">Nombre completo</label>
                        <input type="text" id="nombre" name="nombre" required maxlength="160" autocomplete="name"
                               value="<c:out value='${empty param.nombre ? cliente.nombreCompleto : param.nombre}' />">
                    </div>
                    <div class="grupo-campo">
                        <label for="correo">Correo para el comprobante</label>
                        <input type="email" id="correo" name="correo" required maxlength="120" autocomplete="email"
                               value="<c:out value='${empty param.correo ? cliente.correo : param.correo}' />">
                    </div>
                    <div class="grupo-campo">
                        <label for="pais">País</label>
                        <select id="pais" name="pais" required autocomplete="country-name">
                            <c:forEach var="p" items="${paises}">
                                <option value="${p}" ${(empty param.pais and p == 'El Salvador') or param.pais == p ? 'selected' : ''}>${p}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="grupo-campo">
                        <label for="direccion">Dirección (opcional)</label>
                        <input type="text" id="direccion" name="direccion" maxlength="200" autocomplete="street-address"
                               value="<c:out value='${param.direccion}' />">
                    </div>
                </div>
            </section>

            <section class="tarjeta">
                <h2 class="checkout-titulo"><span>2</span> Método de pago</h2>
                <%-- Única forma de pago del portal: tarjeta (seleccionada por defecto, sin otras opciones). --%>
                <div class="metodo-pago">
                    <span class="metodo-radio" aria-hidden="true"></span>
                    <div>
                        <strong>Tarjeta de crédito o débito</strong>
                        <span class="ayuda">Única forma de pago en línea</span>
                    </div>
                    <div class="marcas-tarjeta" aria-hidden="true"><span>VISA</span><span>Mastercard</span><span>AMEX</span></div>
                </div>

                <div class="tarjeta-pago">
                    <div class="tarjeta-visual" aria-hidden="true">
                        <div class="chip"></div>
                        <div class="tv-numero" id="tvNumero">•••• •••• •••• ••••</div>
                        <div class="tv-pie">
                            <div><small>Titular</small><span id="tvTitular">NOMBRE APELLIDO</span></div>
                            <div><small>Vence</small><span id="tvVence">MM/AA</span></div>
                        </div>
                    </div>
                    <div class="campos-tarjeta">
                        <div class="grupo-campo">
                            <label for="numero">Número de tarjeta</label>
                            <input type="text" id="numero" name="numero" required inputmode="numeric"
                                   autocomplete="cc-number" placeholder="1234 5678 9012 3456">
                        </div>
                        <div class="grupo-campo">
                            <label for="titular">Nombre del titular</label>
                            <input type="text" id="titular" name="titular" required autocomplete="cc-name"
                                   placeholder="Como aparece en la tarjeta">
                        </div>
                        <div class="acceso-par">
                            <div class="grupo-campo">
                                <label for="vencimiento">Vencimiento</label>
                                <input type="text" id="vencimiento" name="vencimiento" required autocomplete="cc-exp" placeholder="MM/AA">
                            </div>
                            <div class="grupo-campo">
                                <label for="cvv">CVV</label>
                                <input type="password" id="cvv" name="cvv" required inputmode="numeric" autocomplete="cc-csc" placeholder="123">
                            </div>
                        </div>
                    </div>
                </div>
                <label class="casilla" style="margin-top:6px">
                    <input type="checkbox" name="terminos" value="si" required>
                    Acepto los términos de compra: los boletos son personales y solo se anulan hasta 24 horas antes de la apertura de puertas.
                </label>
            </section>
        </form>

        <%-- Resumen del pedido --%>
        <aside class="checkout-resumen tarjeta">
            <div class="resumen-evento">
                <img alt="" src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
                <div>
                    <strong><c:out value="${evento.nombre}" /></strong>
                    <span class="ayuda">${evento.fechaLarga} · ${evento.horarioTexto}</span>
                    <span class="ayuda"><c:out value="${evento.recinto}" /></span>
                </div>
            </div>
            <ul class="resumen-lineas">
                <c:forEach var="a" items="${reserva}">
                    <li><span><c:out value="${a.etiqueta}" /></span><span>${a.precioTexto}</span></li>
                </c:forEach>
            </ul>
            <ul class="resumen-lineas totales">
                <li><span>Subtotal</span><span>${total}</span></li>
                <li><span>Cargo por servicio</span><span>$0.00</span></li>
                <li class="total"><span>Total</span><span>${total}</span></li>
            </ul>
            <p class="resumen-tiempo">Su reserva vence en <strong id="cuentaRegresiva" data-segundos="${segundosRestantes}">--:--</strong></p>
            <button type="submit" form="formPago" class="boton acento acento-grande" id="botonPagar">Pagar ${total}</button>
            <p class="nota-segura">🔒 Pago simulado: no se realiza ningún cobro y los datos de la tarjeta no se guardan.</p>
            <a class="ayuda" href="${ctx}/cliente/comprar?evento=${evento.id}">← Volver a los asientos</a>
        </aside>
    </div>
</main>

<div class="procesando" id="procesando" hidden>
    <div class="procesando-caja">
        <div class="girador"></div>
        <strong>Procesando pago…</strong>
        <span>No cierre esta ventana.</span>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
<script>
    (function () {
        var form = document.getElementById('formPago');
        var numero = document.getElementById('numero');
        var titular = document.getElementById('titular');
        var vence = document.getElementById('vencimiento');

        // Solo formato visual (espacios cada 4 y barra en el vencimiento); no se valida la longitud.
        numero.addEventListener('input', function () {
            var limpio = numero.value.replace(/\s+/g, '');
            numero.value = limpio.replace(/(.{4})/g, '$1 ').trim();
            document.getElementById('tvNumero').textContent = numero.value || '•••• •••• •••• ••••';
        });
        titular.addEventListener('input', function () {
            document.getElementById('tvTitular').textContent = titular.value.toUpperCase() || 'NOMBRE APELLIDO';
        });
        vence.addEventListener('input', function () {
            var v = vence.value.replace(/[^0-9]/g, '');
            if (v.length > 2) { v = v.slice(0, 2) + '/' + v.slice(2); }
            vence.value = v;
            document.getElementById('tvVence').textContent = vence.value || 'MM/AA';
        });

        // Simulación de la pasarela: pantalla "Procesando pago…" y luego se envía.
        var enviado = false;
        form.addEventListener('submit', function (e) {
            if (enviado) { return; }
            e.preventDefault();
            enviado = true;
            document.getElementById('procesando').hidden = false;
            document.getElementById('botonPagar').disabled = true;
            setTimeout(function () { HTMLFormElement.prototype.submit.call(form); }, 1600);
        });

        // Tiempo de la reserva (RF-18): al vencer se vuelve al mapa.
        var el = document.getElementById('cuentaRegresiva');
        var restante = parseInt(el.dataset.segundos, 10);
        (function pintar() {
            var m = Math.floor(restante / 60), s = restante % 60;
            el.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
            el.classList.toggle('urgente', restante <= 60);
            if (restante <= 0) { location.href = '${ctx}/cliente/comprar?evento=${evento.id}'; return; }
            restante--;
            setTimeout(pintar, 1000);
        })();
    })();
</script>
</body>
</html>
