<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="Mapa de asientos" />
<c:set var="paginaActiva" value="eventos" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="container-xl py-4">
    <nav aria-label="Ruta"><ol class="breadcrumb small mb-2"><li class="breadcrumb-item"><a href="${ctx}/organizador/eventos">Eventos</a></li><li class="breadcrumb-item"><a href="${ctx}/organizador/eventos?accion=ver&id=${evento.id}"><c:out value="${evento.nombre}" /></a></li><li class="breadcrumb-item active" aria-current="page">Mapa</li></ol></nav>
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
        <div>
            <h1 class="h3 mb-1">Mapa de asientos <span class="badge rounded-pill etiqueta ${evento.estado.clase}">${evento.estado.bd}</span></h1>
            <p class="text-body-secondary mb-0"><c:out value="${evento.nombre}" /> · <c:out value="${evento.recinto}" /> · ${evento.fechaInicioTexto}</p>
        </div>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <div class="row row-cols-2 row-cols-md-3 row-cols-xl-4 g-3 mb-4 indicadores">
        <div class="col"><div class="card h-100 indicador verde"><div class="card-body"><div class="valor">${conteo['Disponible']}</div><div class="nombre">Disponibles</div></div></div></div>
        <div class="col"><div class="card h-100 indicador amarillo"><div class="card-body"><div class="valor">${conteo['Reservado']}</div><div class="nombre">Reservados</div></div></div></div>
        <div class="col"><div class="card h-100 indicador acento"><div class="card-body"><div class="valor">${conteo['Vendido']}</div><div class="nombre">Vendidos</div></div></div></div>
        <div class="col"><div class="card h-100 indicador"><div class="card-body"><div class="valor">${conteo['Bloqueado']}</div><div class="nombre">Bloqueados</div></div></div></div>
    </div>

    <section class="card card-body shadow-sm mb-4">
        <div class="leyenda">
            <span><i class="Disponible"></i> Disponible</span>
            <span><i class="Reservado"></i> Reservado</span>
            <span><i class="Vendido"></i> Vendido</span>
            <span><i class="Bloqueado"></i> Bloqueado</span>
            <c:if test="${puedeBloquear}">
                <span class="small text-body-secondary">Clic en un asiento disponible para bloquearlo (cortesías, producción); clic en uno bloqueado para liberarlo.</span>
            </c:if>
        </div>
        <div class="escenario">ESCENARIO / CANCHA</div>

        <c:forEach var="z" items="${zonas}">
            <div class="zona-mapa">
                <h3><c:out value="${z.nombre}" /> <span class="small text-body-secondary">· ${empty z.precio ? 'sin precio' : z.precioTexto}</span></h3>
                <form method="post" action="${ctx}/organizador/mapa" class="filas">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="evento" value="${evento.id}">
                    <c:forEach var="fila" items="${z.filas}">
                        <div class="fila-asientos">
                            <span class="letra">${fila.letra}</span>
                            <c:forEach var="a" items="${fila.asientos}">
                                <c:choose>
                                    <c:when test="${puedeBloquear and (a.estado == 'Disponible' or a.estado == 'Bloqueado')}">
                                        <button type="submit" name="cambio"
                                                value="${a.idEventoAsiento}:${a.estado == 'Disponible' ? 'si' : 'no'}"
                                                class="asiento ${a.estado}" title="${a.etiqueta} · ${a.estado}">${a.numero}</button>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="asiento ${a.estado}" title="${a.etiqueta} · ${a.estado}">${a.numero}</span>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>
                    </c:forEach>
                </form>
            </div>
        </c:forEach>
        <c:if test="${empty zonas}">
            <div class="alert alert-info">El mapa se genera al publicar el evento.</div>
        </c:if>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
