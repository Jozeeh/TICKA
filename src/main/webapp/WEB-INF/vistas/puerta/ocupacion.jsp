<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Ocupación en vivo" />
<c:set var="paginaActiva" value="ocupacion" />
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
            <h1>Ocupación en vivo</h1>
            <p class="subtitulo">Ingresados frente a vendidos por zona. Se actualiza sola cada 10 segundos. <span class="actualizado" id="actualizado"></span></p>
        </div>
        <a class="boton" href="${ctx}/puerta/validar">Validar boletos</a>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <form method="get" action="${ctx}/puerta/ocupacion" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:320px">
            <label for="evento">Evento</label>
            <select id="evento" name="evento" onchange="this.form.submit()">
                <option value="">Seleccione...</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${evento.id == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /> · ${ev.fechaInicioTexto}</option>
                </c:forEach>
            </select>
        </div>
    </form>

    <c:if test="${not empty evento}">
        <section class="indicadores">
            <div class="indicador"><div class="valor" id="totVendidos">—</div><div class="nombre">Boletos vendidos</div></div>
            <div class="indicador verde"><div class="valor" id="totIngresados">—</div><div class="nombre">Ingresaron</div></div>
            <div class="indicador acento"><div class="valor" id="totPorcentaje">—</div><div class="nombre">Del público ya está dentro</div></div>
        </section>
        <section class="tarjeta tabla-contenedor">
            <table class="tabla">
                <thead><tr><th>Zona</th><th class="numero">Vendidos</th><th class="numero">Ingresaron</th><th class="numero">Faltan</th><th>Avance</th></tr></thead>
                <tbody id="cuerpoZonas">
                <c:forEach var="z" items="${zonas}">
                    <tr>
                        <td><strong><c:out value="${z.zona}" /></strong></td>
                        <td class="numero">${z.vendidos}</td>
                        <td class="numero">${z.ingresados}</td>
                        <td class="numero">${z.pendientes}</td>
                        <td style="min-width:180px">${z.porcentaje}% <div class="barra"><span style="width:${z.porcentaje}%"></span></div></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty zonas}">
                    <tr><td colspan="5" class="vacio">Este evento no tiene boletos vendidos.</td></tr>
                </c:if>
                </tbody>
            </table>
        </section>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
<c:if test="${not empty evento}">
<script>
    // Actualización automática con fetch (AJAX, RF-28): sin recargar la página.
    (function () {
        var url = '${ctx}/puerta/ocupacion?formato=json&evento=${evento.id}';
        function texto(valor) { return document.createTextNode(String(valor)); }
        function celda(valor, numero) {
            var td = document.createElement('td');
            if (numero) { td.className = 'numero'; }
            td.appendChild(texto(valor));
            return td;
        }
        function pintar(d) {
            document.getElementById('totVendidos').textContent = d.vendidos;
            document.getElementById('totIngresados').textContent = d.ingresados;
            document.getElementById('totPorcentaje').textContent = d.porcentaje + '%';
            document.getElementById('actualizado').textContent = '(actualizado ' + d.hora + ')';
            var cuerpo = document.getElementById('cuerpoZonas');
            if (d.zonas.length === 0) { return; }
            cuerpo.innerHTML = '';
            d.zonas.forEach(function (z) {
                var tr = document.createElement('tr');
                var nombre = document.createElement('td');
                var fuerte = document.createElement('strong');
                fuerte.appendChild(texto(z.zona));
                nombre.appendChild(fuerte);
                tr.appendChild(nombre);
                tr.appendChild(celda(z.vendidos, true));
                tr.appendChild(celda(z.ingresados, true));
                tr.appendChild(celda(Math.max(0, z.vendidos - z.ingresados), true));
                var avance = document.createElement('td');
                avance.style.minWidth = '180px';
                avance.appendChild(texto(z.porcentaje + '% '));
                var barra = document.createElement('div');
                barra.className = 'barra';
                var relleno = document.createElement('span');
                relleno.style.width = z.porcentaje + '%';
                barra.appendChild(relleno);
                avance.appendChild(barra);
                tr.appendChild(avance);
                cuerpo.appendChild(tr);
            });
        }
        function actualizar() {
            fetch(url, {credentials: 'same-origin', headers: {'Accept': 'application/json'}})
                .then(function (r) { return r.ok ? r.json() : null; })
                .then(function (d) { if (d) { pintar(d); } })
                .catch(function () { /* se reintenta en la siguiente vuelta */ });
        }
        actualizar();
        setInterval(actualizar, 10000);
    })();
</script>
</c:if>
</body>
</html>
