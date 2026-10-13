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

    <%-- Formulario de zona (nueva o edición) --%>
    <section class="tarjeta">
        <h2 style="margin-top:0">${empty zona.id ? 'Agregar zona' : 'Editar zona'}</h2>
        <form method="post" action="${ctx}/organizador/zonas" class="filtros">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="recinto" value="${recinto.id}">
            <input type="hidden" name="id" value="${zona.id}">
            <div class="grupo-campo">
                <label for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre" maxlength="60" required
                       placeholder="Platea, Palco, Gradas..." value="<c:out value='${zona.nombre}' />">
            </div>
            <div class="grupo-campo" style="min-width:300px">
                <label for="descripcion">Descripción</label>
                <input type="text" id="descripcion" name="descripcion" maxlength="200"
                       value="<c:out value='${zona.descripcion}' />">
            </div>
            <button type="submit" class="boton verde">Guardar zona</button>
            <c:if test="${not empty zona.id}">
                <a class="boton gris" href="${ctx}/organizador/zonas?recinto=${recinto.id}">Cancelar</a>
            </c:if>
        </form>
    </section>

    <div class="leyenda">
        <span><i class="Activo"></i> Asiento activo</span>
        <span><i class="Inactivo"></i> Inhabilitado</span>
        <span class="ayuda">Haga clic en un asiento para habilitarlo o inhabilitarlo (los asientos no se borran, RN-14).</span>
    </div>

    <c:forEach var="z" items="${zonas}">
        <section class="tarjeta zona-mapa">
            <div class="cabecera-pagina" style="margin-bottom:10px">
                <div>
                    <h3><c:out value="${z.nombre}" /></h3>
                    <span class="ayuda"><c:out value="${z.descripcion}" /> · ${z.asientosActivos} activos de ${z.totalAsientos}</span>
                </div>
                <div class="acciones-linea">
                    <a class="boton amarillo chico" href="${ctx}/organizador/zonas?recinto=${recinto.id}&accion=editar&zona=${z.id}">Editar</a>
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
            </div>

            <form method="post" action="${ctx}/organizador/zonas" class="filtros">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="accion" value="generar">
                <input type="hidden" name="recinto" value="${recinto.id}">
                <input type="hidden" name="id" value="${z.id}">
                <div class="grupo-campo" style="min-width:90px">
                    <label>Fila desde</label>
                    <input type="text" name="desde" maxlength="1" required placeholder="A">
                </div>
                <div class="grupo-campo" style="min-width:90px">
                    <label>Fila hasta</label>
                    <input type="text" name="hasta" maxlength="1" required placeholder="J">
                </div>
                <div class="grupo-campo" style="min-width:130px">
                    <label>Asientos por fila</label>
                    <input type="number" name="porFila" min="1" max="50" required placeholder="20">
                </div>
                <button type="submit" class="boton">Generar asientos</button>
            </form>

            <c:choose>
                <c:when test="${empty z.filas}">
                    <p class="ayuda">Esta zona todavía no tiene asientos.</p>
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
