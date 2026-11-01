<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Validar boletos" />
<c:set var="paginaActiva" value="validar" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <h1>Validar boletos</h1>
    <p class="subtitulo">Escanee el QR (con lector o con la cámara) o escriba el código. Cada boleto entra una sola vez<c:if test="${reentrada}"> (re-entrada habilitada)</c:if>.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Evento de trabajo (se recuerda en la sesión) --%>
    <form method="get" action="${ctx}/puerta/validar" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:320px">
            <label for="evento">Evento que controla</label>
            <select id="evento" name="evento" onchange="this.form.submit()">
                <option value="">Seleccione...</option>
                <c:forEach var="ev" items="${eventos}">
                    <option value="${ev.id}" ${evento.id == ev.id ? 'selected' : ''}>
                        <c:out value="${ev.nombre}" /> · ${ev.fechaInicioTexto} · ${ev.estado.bd}
                    </option>
                </c:forEach>
            </select>
        </div>
        <c:if test="${not empty evento}">
            <span class="ayuda" style="align-self:center">Puertas: ${evento.aperturaPuertasTexto} · Fin: ${evento.fechaFinTexto}</span>
        </c:if>
    </form>

    <c:if test="${not empty evento}">
        <div class="puerta-rejilla">
            <div>
                <%-- Resultado de la última lectura (RF-26, RF-27) --%>
                <c:if test="${not empty resultado}">
                    <div class="resultado-validacion ${resultado.valido ? 'valido' : 'rechazado'}" id="resultado">
                        <div class="estado">${resultado.valido ? 'VÁLIDO' : 'RECHAZADO'}</div>
                        <c:choose>
                            <c:when test="${resultado.valido}">
                                <div class="asiento-grande"><c:out value="${resultado.zona}" /> · ${resultado.asiento}</div>
                                <p><c:out value="${resultado.cliente}" /></p>
                            </c:when>
                            <c:otherwise>
                                <div class="asiento-grande"><c:out value="${resultado.motivo}" /></div>
                                <c:if test="${not empty resultado.asiento}">
                                    <p>Asiento del boleto: <c:out value="${resultado.zona}" /> · ${resultado.asiento}</p>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                        <p><code><c:out value="${resultado.codigo}" /></code> · ${resultado.hora}</p>
                    </div>
                </c:if>

                <section class="tarjeta">
                    <h2 style="margin-top:0">Leer código</h2>
                    <form method="post" action="${ctx}/puerta/validar" id="formValidar">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="validar">
                        <div class="rejilla-campos">
                            <div class="grupo-campo completo">
                                <label for="codigo">Código del boleto</label>
                                <input type="text" id="codigo" name="codigo" class="campo-codigo" required autofocus
                                       autocomplete="off" maxlength="60" placeholder="TK-...">
                            </div>
                            <div class="grupo-campo">
                                <label for="puerta">Puerta</label>
                                <input type="text" id="puerta" name="puerta" required maxlength="20" value="<c:out value='${puerta}' />">
                            </div>
                        </div>
                        <div class="acciones-linea">
                            <button type="submit" class="boton verde">Validar</button>
                            <button type="button" class="boton borde" id="botonCamara" hidden>Escanear con la cámara</button>
                        </div>
                    </form>
                    <div class="lector-camara" id="lectorCamara" hidden style="margin-top:12px">
                        <video id="video" playsinline muted></video>
                        <p class="ayuda">Apunte la cámara al código QR del boleto.</p>
                    </div>
                </section>

                <%-- Búsqueda por documento (RF-25) --%>
                <section class="tarjeta">
                    <h2 style="margin-top:0">Buscar por documento</h2>
                    <form method="get" action="${ctx}/puerta/validar" class="acciones-linea">
                        <input type="text" name="documento" placeholder="00000000-0" required style="max-width:220px"
                               value="<c:out value='${documento}' />">
                        <button type="submit" class="boton">Buscar boletos</button>
                    </form>
                    <c:if test="${not empty documento}">
                        <table class="tabla" style="margin-top:12px">
                            <thead><tr><th>Cliente</th><th>Asiento</th><th>Estado</th><th></th></tr></thead>
                            <tbody>
                            <c:forEach var="b" items="${boletosDocumento}">
                                <tr>
                                    <td><c:out value="${b.cliente}" /></td>
                                    <td><c:out value="${b.zona}" /> · ${b.asiento}</td>
                                    <td><c:out value="${b.estado}" /></td>
                                    <td>
                                        <c:if test="${b.estado == 'Emitida'}">
                                            <form method="post" action="${ctx}/puerta/validar" class="linea">
                                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                                <input type="hidden" name="accion" value="validar">
                                                <input type="hidden" name="codigo" value="${b.codigo}">
                                                <input type="hidden" name="puerta" value="<c:out value='${puerta}' />">
                                                <button type="submit" class="boton verde chico">Dar ingreso</button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty boletosDocumento}">
                                <tr><td colspan="4" class="vacio">Ese documento no tiene boletos para este evento.</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </c:if>
                </section>
            </div>

            <%-- Bitácora de la puerta con anulación de ingresos (RF-27, RF-29) --%>
            <section class="tarjeta tabla-contenedor">
                <div class="cabecera-pagina" style="margin-bottom:8px">
                    <h2 style="margin:0">Últimos accesos</h2>
                    <a class="boton borde chico" href="${ctx}/puerta/ocupacion">Ver ocupación</a>
                </div>
                <table class="tabla">
                    <thead><tr><th>Hora</th><th>Resultado</th><th>Asiento / motivo</th><th>Puerta</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="a" items="${accesos}">
                        <tr>
                            <td>${a.horaTexto}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${a.anulado}"><span class="etiqueta Borrador">Anulado</span></c:when>
                                    <c:when test="${a.valido}"><span class="etiqueta activo">Válido</span></c:when>
                                    <c:otherwise><span class="etiqueta inactivo">Rechazado</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:out value="${a.valido ? a.asiento : a.motivoRechazo}" />
                                <c:if test="${a.anulado}"><br><span class="ayuda"><c:out value="${a.motivoAnulacion}" /></span></c:if>
                            </td>
                            <td><c:out value="${a.puerta}" /></td>
                            <td>
                                <c:if test="${a.valido and not a.anulado}">
                                    <form method="post" action="${ctx}/puerta/validar" class="acciones-linea"
                                          data-confirmar="¿Anular este ingreso?" data-peligro="si" data-boton="Sí, anular"
                                          data-detalle="El boleto volverá a quedar disponible para ingresar.">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                        <input type="hidden" name="accion" value="anular">
                                        <input type="hidden" name="acceso" value="${a.id}">
                                        <input type="text" name="motivo" required minlength="5" maxlength="200" placeholder="Motivo" style="width:130px">
                                        <button type="submit" class="boton rojo chico">Anular</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty accesos}">
                        <tr><td colspan="5" class="vacio">Todavía no hay accesos en este evento.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </section>
        </div>
    </c:if>
    <c:if test="${empty evento}">
        <div class="tarjeta vacio">Seleccione el evento que está controlando para empezar a validar.</div>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
<script>
    // Lectura con la cámara del celular o tableta (BarcodeDetector del navegador, si existe).
    // Funciona en Chrome/Edge para Android y requiere HTTPS o localhost.
    (function () {
        var boton = document.getElementById('botonCamara');
        if (!boton || !('BarcodeDetector' in window) || !navigator.mediaDevices) {
            return;
        }
        boton.hidden = false;
        boton.addEventListener('click', function () {
            var lector = document.getElementById('lectorCamara');
            var video = document.getElementById('video');
            var detector = new BarcodeDetector({formats: ['qr_code']});
            navigator.mediaDevices.getUserMedia({video: {facingMode: 'environment'}}).then(function (stream) {
                lector.hidden = false;
                video.srcObject = stream;
                video.play();
                var buscar = function () {
                    detector.detect(video).then(function (codigos) {
                        if (codigos.length > 0) {
                            stream.getTracks().forEach(function (t) { t.stop(); });
                            document.getElementById('codigo').value = codigos[0].rawValue;
                            document.getElementById('formValidar').submit();
                        } else {
                            requestAnimationFrame(buscar);
                        }
                    }).catch(function () { requestAnimationFrame(buscar); });
                };
                buscar();
            }).catch(function () {
                if (window.avisar) { avisar('Sin cámara', 'No se pudo abrir la cámara. Escriba el código.'); }
            });
        });
    })();
</script>
</body>
</html>
