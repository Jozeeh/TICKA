<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Incidencias" />
<c:set var="paginaActiva" value="incidencias" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <h1>Incidencias</h1>
    <p class="subtitulo">Registre los incidentes del evento y deles seguimiento: Abierta → En atención → Cerrada.</p>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Registrar (RF-32) --%>
    <section class="tarjeta formulario">
        <h2 style="margin-top:0">Nueva incidencia</h2>
        <form method="post" action="${ctx}/puerta/incidencias">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="registrar">
            <div class="rejilla-campos">
                <div class="grupo-campo">
                    <label for="nEvento">Evento</label>
                    <select id="nEvento" name="evento" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="ev" items="${eventos}">
                            <option value="${ev.id}" ${eventoTrabajo == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /> · ${ev.fechaTexto}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="nTipo">Tipo</label>
                    <select id="nTipo" name="tipo" required>
                        <c:forEach var="t" items="${tipos}"><option value="${t[0]}">${t[1]}</option></c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="nGravedad">Gravedad</label>
                    <select id="nGravedad" name="gravedad" required>
                        <c:forEach var="g" items="${gravedades}"><option value="${g}" ${g == 'Media' ? 'selected' : ''}>${g}</option></c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="nPuerta">Puerta o ubicación</label>
                    <input type="text" id="nPuerta" name="puerta" maxlength="20" value="<c:out value='${puertaTrabajo}' />">
                </div>
                <div class="grupo-campo completo">
                    <label for="nDescripcion">Descripción</label>
                    <textarea id="nDescripcion" name="descripcion" required minlength="5" maxlength="500"></textarea>
                </div>
            </div>
            <div class="acciones-formulario"><button type="submit" class="boton verde">Registrar incidencia</button></div>
        </form>
    </section>

    <%-- Consultar (RF-34) --%>
    <form method="get" action="${ctx}/puerta/incidencias" class="tarjeta filtros">
        <div class="grupo-campo" style="min-width:240px">
            <label for="evento">Evento</label>
            <select id="evento" name="evento">
                <option value="">Todos</option>
                <c:forEach var="ev" items="${eventosFiltro}">
                    <option value="${ev.id}" ${idEvento == ev.id ? 'selected' : ''}><c:out value="${ev.nombre}" /></option>
                </c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="tipo">Tipo</label>
            <select id="tipo" name="tipo">
                <option value="">Todos</option>
                <c:forEach var="t" items="${tipos}"><option value="${t[0]}" ${tipoFiltro == t[0] ? 'selected' : ''}>${t[1]}</option></c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="gravedad">Gravedad</label>
            <select id="gravedad" name="gravedad">
                <option value="">Todas</option>
                <c:forEach var="g" items="${gravedades}"><option value="${g}" ${gravedadFiltro == g ? 'selected' : ''}>${g}</option></c:forEach>
            </select>
        </div>
        <div class="grupo-campo">
            <label for="estado">Estado</label>
            <select id="estado" name="estado">
                <option value="">Todos</option>
                <c:forEach var="e" items="${estados}"><option value="${e}" ${estadoFiltro == e ? 'selected' : ''}>${e == 'En atencion' ? 'En atención' : e}</option></c:forEach>
            </select>
        </div>
        <button type="submit" class="boton">Filtrar</button>
        <a class="boton gris" href="${ctx}/puerta/incidencias">Limpiar</a>
    </form>

    <section class="tarjeta tabla-contenedor">
        <table class="tabla">
            <thead><tr><th>Fecha</th><th>Evento</th><th>Tipo</th><th>Gravedad</th><th>Descripción</th><th>Estado</th><th>Seguimiento</th></tr></thead>
            <tbody>
            <c:forEach var="i" items="${incidencias}">
                <tr>
                    <td>${i.fechaHoraTexto}<br><span class="ayuda"><c:out value="${i.puerta}" /></span></td>
                    <td><c:out value="${i.evento}" /></td>
                    <td><c:out value="${i.tipoTexto}" /></td>
                    <td><span class="etiqueta ${i.gravedad == 'Alta' ? 'inactivo' : i.gravedad == 'Media' ? 'bloqueado' : 'Borrador'}">${i.gravedad}</span></td>
                    <td style="max-width:320px"><c:out value="${i.descripcion}" />
                        <c:if test="${not empty i.observaciones}"><br><span class="ayuda">Obs.: <c:out value="${i.observaciones}" /></span></c:if></td>
                    <td><span class="etiqueta ${i.estado == 'Cerrada' ? 'activo' : i.estado == 'Abierta' ? 'inactivo' : 'Encurso'}">${i.estadoTexto}</span></td>
                    <td>
                        <c:if test="${i.estado != 'Cerrada'}">
                            <form method="post" action="${ctx}/puerta/incidencias" class="acciones-linea">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="accion" value="estado">
                                <input type="hidden" name="id" value="${i.id}">
                                <input type="text" name="observaciones" maxlength="500" placeholder="Observaciones" style="width:150px">
                                <c:if test="${i.estado == 'Abierta'}">
                                    <button type="submit" name="estado" value="En atencion" class="boton chico">En atención</button>
                                </c:if>
                                <button type="submit" name="estado" value="Cerrada" class="boton verde chico">Cerrar</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty incidencias}">
                <tr><td colspan="7" class="vacio">No hay incidencias con esos filtros.</td></tr>
            </c:if>
            </tbody>
        </table>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
