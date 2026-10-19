<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Zonas y asientos" />
<c:set var="paginaActiva" value="recintos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/organizador/recintos">Recintos</a> › <c:out value="${recinto.nombre}" /></div>
    <div class="cabecera-pagina">
        <div>
            <h1><c:out value="${recinto.nombre}" /></h1>
            <p class="subtitulo"><c:out value="${recinto.tipo}" /> · ${recinto.totalZonas} zonas ·
                ${recinto.totalAsientos} asientos activos</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <%-- Nueva zona: nombre, descripción y distribución de asientos en un solo paso --%>
    <section class="tarjeta">
        <h2 style="margin-top:0">Agregar zona</h2>
        <form method="post" action="${ctx}/organizador/zonas" class="form-zona">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="recinto" value="${recinto.id}">
            <div class="grupo-campo">
                <label>Nombre</label>
                <input type="text" name="nombre" maxlength="60" required placeholder="Platea, Palco, Gradas...">
            </div>
            <div class="grupo-campo">
                <label>Descripción</label>
                <input type="text" name="descripcion" maxlength="200" placeholder="Opcional">
            </div>
            <div class="grupo-campo corto">
                <label>Fila desde</label>
                <input type="text" name="desde" maxlength="1" required placeholder="A">
            </div>
            <div class="grupo-campo corto">
                <label>Fila hasta</label>
                <input type="text" name="hasta" maxlength="1" required placeholder="J">
            </div>
            <div class="grupo-campo corto">
                <label>Asientos por fila</label>
                <input type="number" name="porFila" min="1" max="50" required placeholder="20">
            </div>
            <button type="submit" class="boton verde">Agregar zona</button>
        </form>
    </section>

    <div class="leyenda">
        <span><i class="Activo"></i> Asiento activo</span>
        <span><i class="Inactivo"></i> Inhabilitado</span>
    </div>

    <c:forEach var="z" items="${zonas}">
        <section class="tarjeta zona-mapa">
            <div class="cabecera-pagina" style="margin-bottom:6px">
                <div>
                    <h3><c:out value="${z.nombre}" /></h3>
                    <span class="ayuda">${z.asientosActivos} activos de ${z.totalAsientos}</span>
                </div>
                <c:if test="${z.totalAsientos == 0}">
                    <form method="post" action="${ctx}/organizador/zonas" class="linea"
                          onsubmit="return confirm('¿Eliminar esta zona?');">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="recinto" value="${recinto.id}">
                        <input type="hidden" name="id" value="${z.id}">
                        <button type="submit" class="boton rojo chico">Eliminar</button>
                    </form>
                </c:if>
            </div>

            <%-- Un solo formulario para los datos de la zona y su distribución --%>
            <form method="post" action="${ctx}/organizador/zonas" class="form-zona">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="guardar">
                <input type="hidden" name="recinto" value="${recinto.id}">
                <input type="hidden" name="id" value="${z.id}">
                <div class="grupo-campo">
                    <label>Nombre</label>
                    <input type="text" name="nombre" maxlength="60" required value="<c:out value='${z.nombre}' />">
                </div>
                <div class="grupo-campo">
                    <label>Descripción</label>
                    <input type="text" name="descripcion" maxlength="200" value="<c:out value='${z.descripcion}' />">
                </div>
                <div class="grupo-campo corto">
                    <label>Fila desde</label>
                    <input type="text" name="desde" maxlength="1" required placeholder="A" value="${z.filaDesde}">
                </div>
                <div class="grupo-campo corto">
                    <label>Fila hasta</label>
                    <input type="text" name="hasta" maxlength="1" required placeholder="J" value="${z.filaHasta}">
                </div>
                <div class="grupo-campo corto">
                    <label>Asientos por fila</label>
                    <input type="number" name="porFila" min="1" max="50" required placeholder="20" value="${z.asientosPorFila}">
                </div>
                <button type="submit" class="boton">Guardar zona</button>
            </form>

            <c:choose>
                <c:when test="${empty z.filas}">
                    <p class="ayuda">Esta zona todavía no tiene asientos: indique las filas y los asientos por fila y pulse Guardar zona.</p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${ctx}/organizador/zonas" class="filas">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="asiento">
                        <input type="hidden" name="recinto" value="${recinto.id}">
                        <c:forEach var="fila" items="${z.filas}">
                            <div class="fila-asientos">
                                <span class="letra">${fila.letra}</span>
                                <c:forEach var="a" items="${fila.asientos}">
                                    <button type="submit" name="cambio" value="${a.id}:${a.activo ? 'no' : 'si'}"
                                            class="asiento ${a.clasePlantilla}"
                                            title="${a.etiqueta} — ${a.activo ? 'clic para inhabilitar' : 'clic para habilitar'}">${a.numero}</button>
                                </c:forEach>
                            </div>
                        </c:forEach>
                    </form>
                </c:otherwise>
            </c:choose>
        </section>
    </c:forEach>
    <c:if test="${empty zonas}">
        <div class="alerta info">Este recinto todavía no tiene zonas. Agregue la primera con el formulario de arriba.</div>
    </c:if>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
