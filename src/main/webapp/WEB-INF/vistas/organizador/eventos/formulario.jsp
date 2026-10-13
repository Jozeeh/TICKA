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
    <p class="subtitulo">Los eventos nuevos quedan en <strong>Borrador</strong> hasta que se definan los precios y se publiquen.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="tarjeta formulario" style="max-width:900px">
        <form method="post" action="${ctx}/organizador/eventos">
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
                    <label for="imagen">Afiche (opcional)</label>
                    <input type="text" id="imagen" name="imagen" maxlength="255"
                           placeholder="Ruta o URL de la imagen" value="<c:out value='${evento.imagen}' />">
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
</body>
</html>
