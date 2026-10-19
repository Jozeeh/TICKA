<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${empty evento.id ? 'Nuevo evento' : 'Editar evento'}" />
<c:set var="paginaActiva" value="eventos" />
<%-- Después de publicar, el recinto queda fijo porque ya existe el mapa de asientos. --%>
<c:set var="recintoFijo" value="${not empty evento.id and evento.estado != 'BORRADOR'}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/organizador/eventos">Eventos</a> › <c:out value="${titulo}" /></div>
    <h1><c:out value="${titulo}" /></h1>
    <p class="subtitulo">Al elegir el recinto aparecen sus zonas para asignar el precio de cada una. Los eventos nuevos quedan en <strong>Borrador</strong> hasta que se publiquen.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta formulario" style="max-width:900px">
        <form method="post" action="${ctx}/organizador/eventos" enctype="multipart/form-data">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="id" value="${evento.id}">
            <div class="rejilla-campos">
                <div class="grupo-campo completo">
                    <label for="nombre">Nombre del evento</label>
                    <input type="text" id="nombre" name="nombre" maxlength="150" required
                           value="<c:out value='${evento.nombre}' />">
                </div>
                <div class="grupo-campo">
                    <label for="tipo">Tipo</label>
                    <select id="tipo" name="tipo" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="t" items="${tipos}">
                            <option value="${t.bd}" ${evento.tipo == t ? 'selected' : ''}>${t.etiqueta}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="categoria">Categoría</label>
                    <select id="categoria" name="categoria">
                        <option value="">Sin categoría</option>
                        <c:forEach var="cat" items="${categorias}">
                            <option value="${cat.id}" ${evento.idCategoria == cat.id ? 'selected' : ''}><c:out value="${cat.nombre}" /></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="recinto">Recinto</label>
                    <select id="recinto" name="recinto" required ${recintoFijo ? 'disabled' : ''}>
                        <option value="">Seleccione...</option>
                        <c:forEach var="r" items="${recintos}">
                            <option value="${r.id}" ${evento.idRecinto == r.id ? 'selected' : ''}>
                                <c:out value="${r.nombre}" /> (${r.totalAsientos} asientos)
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${recintoFijo}">
                        <input type="hidden" name="recinto" value="${evento.idRecinto}">
                        <span class="ayuda">El recinto no se puede cambiar después de publicar.</span>
                    </c:if>
                </div>
                <%-- Precios por zona (RF-05): se muestran las zonas del recinto elegido. --%>
                <div class="grupo-campo completo">
                    <label>Precios por zona (USD)</label>
                    <p id="sinRecinto" class="ayuda">Seleccione un recinto para asignar el precio de cada zona.</p>
                    <c:forEach var="r" items="${recintos}">
                        <div class="precios-recinto" data-recinto="${r.id}" hidden>
                            <c:set var="zonasRecinto" value="${preciosPorRecinto[r.id]}" />
                            <c:choose>
                                <c:when test="${empty zonasRecinto}">
                                    <div class="alerta aviso">Este recinto no tiene zonas. Agréguelas en
                                        <a href="${ctx}/organizador/zonas?recinto=${r.id}">Zonas y asientos</a>.</div>
                                </c:when>
                                <c:otherwise>
                                    <table class="tabla">
                                        <thead><tr><th>Zona</th><th class="numero">Asientos activos</th><th>Precio (USD)</th></tr></thead>
                                        <tbody>
                                        <c:forEach var="pz" items="${zonasRecinto}">
                                            <tr>
                                                <td><c:out value="${pz.zona}" /></td>
                                                <td class="numero">${pz.asientosActivos}</td>
                                                <td style="max-width:180px">
                                                    <input type="number" name="precio_${pz.idZona}" min="0" max="99999.99" step="0.01"
                                                           value="${pz.precio}" placeholder="0.00"
                                                           data-obligatorio="${pz.asientosActivos > 0}">
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:forEach>
                </div>
                <div class="grupo-campo completo">
                    <label for="participantes">Participantes</label>
                    <input type="text" id="participantes" name="participantes" maxlength="200"
                           placeholder="Artista, elenco o equipos (ej. Águilas FC vs. Tigres del Norte)"
                           value="<c:out value='${evento.participantes}' />">
                </div>
                <div class="grupo-campo completo">
                    <label for="descripcion">Descripción</label>
                    <textarea id="descripcion" name="descripcion" maxlength="2000"><c:out value="${evento.descripcion}" /></textarea>
                </div>
                <div class="grupo-campo">
                    <label for="aperturaPuertas">Apertura de puertas</label>
                    <input type="datetime-local" id="aperturaPuertas" name="aperturaPuertas" required
                           value="${evento.aperturaPuertasEntrada}">
                </div>
                <div class="grupo-campo">
                    <label for="fechaInicio">Inicio</label>
                    <input type="datetime-local" id="fechaInicio" name="fechaInicio" required
                           value="${evento.fechaInicioEntrada}">
                </div>
                <div class="grupo-campo">
                    <label for="fechaFin">Fin</label>
                    <input type="datetime-local" id="fechaFin" name="fechaFin" required
                           value="${evento.fechaFinEntrada}">
                </div>
                <div class="grupo-campo completo">
                    <label for="afiche">Afiche (opcional)</label>
                    <div class="campo-afiche">
                        <img id="vistaAfiche" class="vista-afiche" alt="Vista previa del afiche"
                             src="${evento.conAfiche ? ctx += '/afiche?evento=' += evento.id : ctx += '/resources/img/sin-imagen.png'}">
                        <div>
                            <input type="file" id="afiche" name="afiche" accept="image/png,image/jpeg,image/webp,image/gif">
                            <span class="ayuda">JPG, PNG, WEBP o GIF de hasta 2 MB. Si no adjunta ninguno, se usa la imagen por defecto.</span>
                            <c:if test="${evento.conAfiche}">
                                <label class="casilla" style="margin-top:8px">
                                    <input type="checkbox" name="quitarAfiche" value="si"> Quitar el afiche actual
                                </label>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
            <div class="acciones-formulario">
                <button type="submit" class="boton verde">Guardar</button>
                <a class="boton gris" href="${ctx}/organizador/eventos${empty evento.id ? '' : '?accion=ver&id='}${evento.id}">Cancelar</a>
            </div>
        </form>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
<script>
    // Muestra solo las zonas del recinto elegido. Los precios de las zonas visibles
    // con asientos son obligatorios; los de otros recintos se ignoran.
    (function () {
        var selector = document.getElementById('recinto');
        function mostrarZonas() {
            var elegido = selector.value;
            document.getElementById('sinRecinto').hidden = elegido !== '';
            document.querySelectorAll('.precios-recinto').forEach(function (grupo) {
                var visible = grupo.dataset.recinto === elegido;
                grupo.hidden = !visible;
                grupo.querySelectorAll('input').forEach(function (campo) {
                    campo.disabled = !visible;
                    campo.required = visible && campo.dataset.obligatorio === 'true';
                });
            });
        }
        selector.addEventListener('change', mostrarZonas);
        mostrarZonas();
    })();

    // Vista previa del afiche y aviso inmediato si pesa más de 2 MB
    // (el servidor vuelve a validar el tamaño y el tipo real de la imagen).
    document.getElementById('afiche').addEventListener('change', function () {
        var archivo = this.files[0];
        if (!archivo) {
            return;
        }
        if (archivo.size > 2 * 1024 * 1024) {
            alert('El afiche no puede pesar más de 2 MB.');
            this.value = '';
            return;
        }
        document.getElementById('vistaAfiche').src = URL.createObjectURL(archivo);
    });
</script>
</body>
</html>
