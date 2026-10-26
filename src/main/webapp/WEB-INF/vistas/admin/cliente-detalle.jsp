<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="titulo" value="${cliente.nombreCompleto}" />
<c:set var="paginaActiva" value="clientes" />
<!DOCTYPE html>
<html lang="es">
<head>
    <%@ include file="/WEB-INF/vistas/plantilla/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/vistas/plantilla/navbar.jspf" %>
<main class="contenedor">
    <div class="migas"><a href="${ctx}/admin/clientes">Clientes</a> › <c:out value="${cliente.nombreCompleto}" /></div>
    <div class="cabecera-pagina">
        <div>
            <h1><c:out value="${cliente.nombreCompleto}" />
                <span class="etiqueta ${cliente.activo ? 'activo' : 'inactivo'}">${cliente.activo ? 'Activo' : 'Inactivo'}</span></h1>
            <p class="subtitulo">Detalle del cliente con todas sus compras y boletos (solo consulta).</p>
        </div>
        <form method="post" action="${ctx}/admin/clientes" class="linea"
              data-confirmar="${cliente.activo ? '¿Desactivar la cuenta del cliente?' : '¿Activar la cuenta del cliente?'}"
              data-detalle="${cliente.activo ? 'Ya no podrá iniciar sesión en el portal.' : ''}"
              data-boton="${cliente.activo ? 'Sí, desactivar' : 'Sí, activar'}" data-peligro="${cliente.activo ? 'si' : 'no'}">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="usuario" value="${cliente.idUsuario}">
            <input type="hidden" name="cliente" value="${cliente.id}">
            <input type="hidden" name="activar" value="${cliente.activo ? 'no' : 'si'}">
            <button type="submit" class="boton ${cliente.activo ? 'rojo' : 'verde'}">${cliente.activo ? 'Desactivar cuenta' : 'Activar cuenta'}</button>
        </form>
    </div>
    <%@ include file="/WEB-INF/vistas/plantilla/alertas.jspf" %>

    <section class="indicadores compactos">
        <div class="indicador"><div class="valor">${cliente.compras}</div><div class="nombre">Compras pagadas</div></div>
        <div class="indicador acento"><div class="valor">${cliente.boletos}</div><div class="nombre">Boletos vigentes</div></div>
        <div class="indicador verde"><div class="valor">${cliente.totalGastadoTexto}</div><div class="nombre">Total gastado</div></div>
    </section>

    <section class="tarjeta">
        <h2 style="margin-top:0">Datos del cliente</h2>
        <dl class="datos">
            <dt>Usuario</dt><dd><c:out value="${cliente.username}" /></dd>
            <dt>Documento</dt><dd><c:out value="${cliente.documento}" /></dd>
            <dt>Correo</dt><dd><c:out value="${cliente.correo}" /></dd>
            <dt>Teléfono</dt><dd><c:out value="${cliente.telefono}" default="—" /></dd>
        </dl>
    </section>

    <section class="tarjeta">
        <h2 style="margin-top:0">Compras</h2>
        <c:if test="${empty compras}">
            <p class="vacio">Este cliente todavía no tiene compras.</p>
        </c:if>
        <c:forEach var="compra" items="${compras}">
            <div class="compra">
                <div class="compra-cabecera">
                    <div>
                        <strong>Venta #${compra.id}</strong>
                        <span> · ${compra.fechaTexto} · <c:out value="${compra.canal}" /> · <c:out value="${compra.formaPago}" /></span>
                    </div>
                    <div>
                        <span class="etiqueta ${compra.estado == 'Pagada' ? 'activo' : 'inactivo'}"><c:out value="${compra.estado}" /></span>
                        <strong style="margin-left:8px">${compra.totalTexto}</strong>
                    </div>
                </div>
                <c:if test="${not empty compra.motivoAnulacion}">
                    <p class="ayuda" style="margin:8px 14px">Motivo de anulación: <c:out value="${compra.motivoAnulacion}" /></p>
                </c:if>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                        <tr><th>Evento</th><th>Fecha del evento</th><th>Zona</th><th>Asiento</th>
                            <th class="numero">Precio</th><th>Estado</th><th>Código</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="b" items="${compra.boletos}">
                            <tr>
                                <td><c:out value="${b.evento}" /></td>
                                <td>${b.fechaEventoTexto}</td>
                                <td><c:out value="${b.zona}" /></td>
                                <td><c:out value="${b.asiento}" /></td>
                                <td class="numero">${b.precioTexto}</td>
                                <td><c:out value="${b.estado}" /></td>
                                <td><code><c:out value="${b.codigoCorto}" /></code></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:forEach>
    </section>
</main>
<%@ include file="/WEB-INF/vistas/plantilla/pie.jspf" %>
</body>
</html>
