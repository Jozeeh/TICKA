<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Elegir asientos" />
<c:set var="paginaActiva" value="cartelera" />
<%-- Pantalla de compra a pantalla completa (sin menú lateral): evento | mapa | zonas y asientos. --%>
<c:set var="sinMenuLateral" value="${true}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body class="cuerpo-compra">
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="compra-app">

    <%-- ============ Columna izquierda: evento, zonas/precios, selección y reserva ============ --%>
    <aside class="compra-col compra-info">
        <a class="ayuda" href="${ctx}/cliente/cartelera">← Volver a la cartelera</a>
        <div class="compra-evento">
            <img alt="" src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
            <div>
                <h1><c:out value="${evento.nombre}" /></h1>
                <p><c:out value="${evento.participantes}" /></p>
                <p class="ayuda">${evento.fechaLarga} · ${evento.horarioTexto}<br><c:out value="${evento.recinto}" /></p>
            </div>
        </div>
        <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>
        <c:if test="${not aLaVenta}">
            <div class="alerta aviso"><c:out value="${motivoSinVenta}" /></div>
        </c:if>

        <%-- Reserva vigente (RF-18): continuar al pago o cancelar --%>
        <c:if test="${not empty reserva and aLaVenta}">
            <section class="bloque-compra reserva-activa">
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

        <%-- Zonas, precios y disponibilidad --%>
        <section class="bloque-compra">
            <h2>Zonas y precios</h2>
            <ul class="lista-precios">
                <c:forEach var="z" items="${zonas}" varStatus="s">
                    <li class="zc${s.index % 7}">
                        <span class="punto-zona"></span>
                        <div>
                            <strong><c:out value="${z.nombre}" /></strong>
                            <span class="ayuda">${z.disponibles} de ${z.total} disponibles</span>
                            <div class="barra"><span style="width:${z.total == 0 ? 0 : z.disponibles * 100 / z.total}%"></span></div>
                        </div>
                        <span class="precio-zona">${empty z.precio ? '—' : z.precioTexto}</span>
                    </li>
                </c:forEach>
            </ul>
        </section>

        <%-- Selección actual (se arma con JavaScript) y botón para reservar --%>
        <c:if test="${aLaVenta}">
            <section class="bloque-compra">
                <h2>Su selección</h2>
                <ul class="resumen-lineas" id="listaSeleccion"><li class="vacio-sel">Elija una zona y marque sus asientos.</li></ul>
                <div class="fila-entre total-seleccion"><span>Total</span><strong id="totalSeleccion">$0.00</strong></div>
                <p class="ayuda">Máximo ${maximo} asientos por compra. Al reservar, quedan apartados ${minutos} minutos.</p>
                <button type="submit" form="formReserva" class="boton acento acento-grande" id="botonReservar" disabled>
                    ${empty reserva ? 'Reservar asientos' : 'Actualizar reserva'}</button>
            </section>
        </c:if>

        <div class="leyenda compacta">
            <span><i class="Disponible"></i> Disponible</span>
            <span><i class="Seleccionado"></i> Su selección</span>
            <span><i class="Reservado"></i> Reservado</span>
            <span><i class="Vendido"></i> Vendido</span>
            <span><i class="Bloqueado"></i> No disponible</span>
        </div>
    </aside>

    <%-- ============ Centro: mapa con todas las zonas (zoom a la zona elegida) ============ --%>
    <section class="compra-col compra-mapa">
        <div class="controles-mapa">
            <button type="button" id="zoomReiniciar" title="Ver todas las zonas" aria-label="Ver todas las zonas">⟲</button>
            <button type="button" id="zoomMas" title="Acercar" aria-label="Acercar">+</button>
            <button type="button" id="zoomMenos" title="Alejar" aria-label="Alejar">−</button>
        </div>
        <span class="etiqueta-mapa" id="etiquetaMapa">Mapa referencial · elija una zona</span>
        <form method="post" action="${ctx}/cliente/comprar" id="formReserva" class="mapa-lienzo">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="evento" value="${evento.id}">
            <input type="hidden" name="accion" value="reservar">
            <div class="mapa-contenido" id="mapaContenido">
                <div class="mapa-zonas">
                    <div class="escenario">ESCENARIO / CANCHA</div>
                    <c:forEach var="z" items="${zonas}" varStatus="s">
                        <div class="zona-bloque zc${s.index % 7}" data-zona="${z.idZona}">
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
                                                           data-zona="${z.idZona}" data-precio="${z.precio}" data-fila="${fila.letra}"
                                                           data-numero="${a.numero}" data-etiqueta="<c:out value='${z.nombre}' /> ${a.etiqueta}"
                                                           ${a.estado == 'Seleccionado' ? 'checked' : ''}>
                                                    <span class="asiento Disponible">${a.numero}</span>
                                                </label>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="asiento ${a.estado == 'Seleccionado' ? 'Reservado' : a.estado}" data-zona="${z.idZona}"
                                                      data-fila="${fila.letra}" data-numero="${a.numero}"
                                                      data-estado="${a.estado}" title="${a.etiqueta} · no disponible">${a.numero}</span>
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

    <%-- ============ Derecha: zonas (arriba) y asientos de la zona elegida (abajo) ============ --%>
    <aside class="compra-col compra-zonas" id="panelZonas">
        <div class="panel-mitad panel-sectores">
            <h2>Seleccionar zona</h2>
            <ul class="lista-sectores">
                <c:forEach var="z" items="${zonas}" varStatus="s">
                    <li>
                        <button type="button" class="sector zc${s.index % 7}" data-zona="${z.idZona}" data-nombre="<c:out value='${z.nombre}' />">
                            <span>
                                <strong><c:out value="${z.nombre}" /></strong>
                                <span class="ayuda">${empty z.precio ? 'Sin precio' : z.precioTexto} · ${z.disponibles} disponibles</span>
                            </span>
                            <span class="chevron" aria-hidden="true">›</span>
                        </button>
                    </li>
                </c:forEach>
            </ul>
        </div>
        <div class="panel-mitad panel-asientos" id="panelAsientos" hidden>
            <div class="fila-entre">
                <h2 id="tituloAsientos">Asientos</h2>
                <button type="button" class="boton borde chico" id="cerrarZona">Ver todas</button>
            </div>
            <label class="casilla"><input type="checkbox" id="soloDisponibles" checked> Mostrar solo disponibles</label>
            <ul class="lista-asientos" id="listaAsientos"></ul>
        </div>
    </aside>
</main>

<script>
    (function () {
        var maximo = ${empty maximo ? 6 : maximo};
        var contenido = document.getElementById('mapaContenido');
        var lienzo = document.getElementById('formReserva');
        var panel = document.getElementById('panelZonas');
        var panelAsientos = document.getElementById('panelAsientos');
        var lista = document.getElementById('listaAsientos');
        var soloDisp = document.getElementById('soloDisponibles');
        var etiquetaMapa = document.getElementById('etiquetaMapa');
        var zonaActual = null;
        var escala = 1, dx = 0, dy = 0;

        function casillas() { return Array.prototype.slice.call(document.querySelectorAll('input[name="asiento"]')); }
        function marcadas() { return casillas().filter(function (c) { return c.checked; }); }
        function aplicar() { contenido.style.transform = 'translate(' + dx + 'px,' + dy + 'px) scale(' + escala + ')'; }

        // Encuadra el elemento dentro del lienzo: zoom a la zona elegida o vista general.
        function encuadrar(el) {
            if (!el) { return; }
            escala = 1; dx = 0; dy = 0;
            contenido.style.transition = 'none';
            aplicar();
            var l = lienzo.getBoundingClientRect(), r = el.getBoundingClientRect();
            var e = Math.min((l.width - 48) / r.width, (l.height - 48) / r.height, 2.4);
            escala = Math.max(e, 0.3);
            dx = (l.width - r.width * escala) / 2 - (r.left - l.left) * escala;
            dy = (l.height - r.height * escala) / 2 - (r.top - l.top) * escala;
            void contenido.offsetWidth;
            contenido.style.transition = '';
            aplicar();
        }
        function vistaGeneral() { encuadrar(contenido.querySelector('.mapa-zonas')); }

        // Elegir zona: zoom en el mapa, las demás en gris y la lista de sus asientos abajo a la derecha.
        function elegirZona(id) {
            zonaActual = id;
            document.querySelectorAll('.zona-bloque').forEach(function (b) {
                b.classList.toggle('enfocada', b.dataset.zona === id);
                b.classList.toggle('atenuada', b.dataset.zona !== id);
            });
            document.querySelectorAll('.sector').forEach(function (s) { s.classList.toggle('activo', s.dataset.zona === id); });
            var sector = document.querySelector('.sector[data-zona="' + id + '"]');
            document.getElementById('tituloAsientos').textContent = 'Asientos · ' + sector.dataset.nombre;
            etiquetaMapa.textContent = sector.dataset.nombre;
            panel.classList.add('dividida');
            panelAsientos.hidden = false;
            pintarLista();
            encuadrar(document.querySelector('.zona-bloque[data-zona="' + id + '"]'));
        }
        function soltarZona() {
            zonaActual = null;
            document.querySelectorAll('.zona-bloque').forEach(function (b) { b.classList.remove('enfocada', 'atenuada'); });
            document.querySelectorAll('.sector').forEach(function (s) { s.classList.remove('activo'); });
            panel.classList.remove('dividida');
            panelAsientos.hidden = true;
            etiquetaMapa.textContent = 'Mapa referencial · elija una zona';
            vistaGeneral();
        }

        // Lista de asientos de la zona. La casilla del mapa es la única fuente: la lista solo la marca o desmarca.
        function pintarLista() {
            lista.innerHTML = '';
            if (!zonaActual) { return; }
            var bloque = document.querySelector('.zona-bloque[data-zona="' + zonaActual + '"]');
            bloque.querySelectorAll('.asiento-sel input, span.asiento[data-estado]').forEach(function (el) {
                var libre = el.tagName === 'INPUT';
                if (!libre && soloDisp.checked) { return; }
                var li = document.createElement('li');
                var b = document.createElement('button');
                b.type = 'button';
                b.className = 'item-asiento' + (libre && el.checked ? ' marcado' : '') + (libre ? '' : ' ocupado');
                b.disabled = !libre;
                var t = document.createElement('span');
                t.textContent = 'Fila ' + el.dataset.fila + ' · Asiento ' + el.dataset.numero;
                var d = document.createElement('span');
                d.className = 'estado-item';
                d.textContent = libre ? (el.checked ? '✓ Seleccionado' : '$' + parseFloat(el.dataset.precio || 0).toFixed(2))
                                      : (el.dataset.estado === 'Vendido' ? 'Vendido'
                                         : el.dataset.estado === 'Bloqueado' ? 'No disponible' : 'Reservado');
                b.appendChild(t);
                b.appendChild(d);
                if (libre) {
                    b.addEventListener('click', function () { alternar(el); });
                }
                li.appendChild(b);
                lista.appendChild(li);
            });
            if (!lista.children.length) {
                var vacio = document.createElement('li');
                vacio.className = 'vacio-sel';
                vacio.textContent = 'No quedan asientos disponibles en esta zona.';
                lista.appendChild(vacio);
            }
        }

        function avisarLimite() {
            if (window.avisar) { avisar('Límite de asientos', 'Puede elegir como máximo ' + maximo + ' asientos por compra.'); }
        }

        function alternar(input) {
            if (!input.checked && marcadas().length >= maximo) { avisarLimite(); return; }
            input.checked = !input.checked;
            actualizar();
        }

        // Resumen "Su selección" (columna izquierda) y lista de la zona.
        function actualizar() {
            var sel = marcadas();
            var ul = document.getElementById('listaSeleccion');
            if (ul) {
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
                    v.textContent = 'Elija una zona y marque sus asientos.';
                    ul.appendChild(v);
                }
                document.getElementById('totalSeleccion').textContent = '$' + total.toFixed(2);
                document.getElementById('botonReservar').disabled = sel.length === 0 || sel.length > maximo;
            }
            pintarLista();
        }

        casillas().forEach(function (c) {
            c.addEventListener('change', function () {
                if (c.checked && marcadas().length > maximo) { c.checked = false; avisarLimite(); }
                if (zonaActual !== c.dataset.zona) { elegirZona(c.dataset.zona); }
                actualizar();
            });
        });
        document.querySelectorAll('.sector, .zona-bloque-titulo').forEach(function (b) {
            b.addEventListener('click', function () { elegirZona(b.dataset.zona); });
        });
        // Clic en una zona que está en gris: se elige esa zona.
        document.querySelectorAll('.zona-bloque').forEach(function (b) {
            b.addEventListener('click', function (e) {
                if (b.classList.contains('atenuada')) { e.preventDefault(); elegirZona(b.dataset.zona); }
            });
        });
        document.getElementById('cerrarZona').addEventListener('click', soltarZona);
        soloDisp.addEventListener('change', pintarLista);
        document.getElementById('zoomMas').addEventListener('click', function () { escala = Math.min(escala * 1.25, 4); aplicar(); });
        document.getElementById('zoomMenos').addEventListener('click', function () { escala = Math.max(escala / 1.25, 0.2); aplicar(); });
        document.getElementById('zoomReiniciar').addEventListener('click', soltarZona);
        window.addEventListener('resize', function () {
            if (zonaActual) {
                encuadrar(document.querySelector('.zona-bloque[data-zona="' + zonaActual + '"]'));
            } else {
                vistaGeneral();
            }
        });

        actualizar();
        // Con una reserva vigente, se abre la zona de sus asientos.
        var primera = marcadas()[0];
        if (primera) { elegirZona(primera.dataset.zona); } else { vistaGeneral(); }

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
