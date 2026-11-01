package sv.edu.itca.ticka.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.VentaDAO;
import sv.edu.itca.ticka.modelo.AsientoSeleccionado;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.VentaAnulable;
import sv.edu.itca.ticka.persistencia.ConexionBD;
import sv.edu.itca.ticka.util.Configuracion;
import sv.edu.itca.ticka.util.GeneradorCodigo;

/**
 * Compra de boletos (M3):
 * - Reservar asientos por N minutos (RF-18, RN-02) con actualización condicional.
 * - Comprar la reserva en una transacción: venta + un boleto por asiento con
 *   código único, y recalcular Agotado (RF-17, RF-20, RF-21, RN-11).
 * - Venta en mostrador del Administrador (RF-24).
 * - Anulación por el cliente dentro del plazo (RF-23, RN-12).
 *
 * Cada operación bloquea la fila del evento (SELECT ... FOR UPDATE), así las
 * compras simultáneas del mismo evento se ordenan y un asiento nunca se vende
 * dos veces (RN-01, RNF-05).
 */
public class CompraServicio {

    public static final Set<String> FORMAS_PAGO = Set.of("Tarjeta", "Transferencia", "Efectivo");

    private final EventoDAO eventoDAO;
    private final VentaDAO ventaDAO;

    public CompraServicio(EventoDAO eventoDAO, VentaDAO ventaDAO) {
        this.eventoDAO = eventoDAO;
        this.ventaDAO = ventaDAO;
    }

    /** Operación dentro de una transacción. */
    private interface Operacion<T> {
        T ejecutar(Connection cn);
    }

    private <T> T enTransaccion(Operacion<T> op, String mensajeError) {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                T resultado = op.ejecutar(cn);
                cn.commit();
                return resultado;
            } catch (ServicioException | DAOException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException(mensajeError, ex);
        }
    }

    /** Bloquea el evento y verifica que esté a la venta (RN-03). */
    private Evento eventoAbierto(Connection cn, long idEvento) {
        Evento e = eventoDAO.buscarParaActualizar(cn, idEvento)
                .orElseThrow(() -> new ServicioException("El evento no existe."));
        String error = ReglasCompra.validarVentaAbierta(e.getEstado(), e.getFechaInicio(), LocalDateTime.now());
        if (error != null) {
            throw new ServicioException(error);
        }
        return e;
    }

    private static void validarCantidad(int cantidad) {
        String error = ReglasCompra.validarCantidad(cantidad, Configuracion.maximoPorCompra());
        if (error != null) {
            throw new ServicioException(error);
        }
    }

    private static List<Long> sinRepetidos(List<Long> ids) {
        return ids.stream().distinct().collect(Collectors.toList());
    }

    /** Mensaje con los asientos que otro cliente tomó primero. */
    private ServicioException asientosTomados(Connection cn, long idEvento, List<Long> fallidos) {
        String etiquetas = ventaDAO.asientosPorIds(cn, idEvento, fallidos).stream()
                .map(AsientoSeleccionado::getEtiqueta).collect(Collectors.joining(", "));
        return new ServicioException("Estos asientos ya no están disponibles: " + etiquetas
                + ". Otro cliente los tomó; elija otros.");
    }

    /**
     * Reserva los asientos elegidos (reemplaza la reserva anterior del cliente
     * en el evento). Si alguno ya no está libre no se reserva ninguno.
     */
    public void reservar(long idEvento, long idCliente, List<Long> idsAsientos) {
        List<Long> ids = sinRepetidos(idsAsientos);
        validarCantidad(ids.size());
        LocalDateTime hasta = LocalDateTime.now().plusMinutes(Configuracion.minutosReserva());
        enTransaccion(cn -> {
            eventoAbierto(cn, idEvento);
            ventaDAO.liberarReservas(cn, idEvento, idCliente);
            List<Long> fallidos = new ArrayList<>();
            for (Long id : ids) {
                if (!ventaDAO.reservar(cn, idEvento, id, idCliente, hasta)) {
                    fallidos.add(id);
                }
            }
            if (!fallidos.isEmpty()) {
                throw asientosTomados(cn, idEvento, fallidos);
            }
            return null;
        }, "No se pudo reservar los asientos");
    }

    public void cancelarReserva(long idEvento, long idCliente) {
        enTransaccion(cn -> ventaDAO.liberarReservas(cn, idEvento, idCliente), "No se pudo liberar la reserva");
    }

    /** Compra la reserva vigente del cliente (canal Web). Devuelve el id de la venta. */
    public long comprar(long idEvento, long idCliente, String formaPago) {
        validarFormaPago(formaPago);
        return enTransaccion(cn -> {
            Evento evento = eventoAbierto(cn, idEvento);
            List<AsientoSeleccionado> asientos = ventaDAO.reservadosParaComprar(cn, idEvento, idCliente);
            if (asientos.isEmpty()) {
                throw new ServicioException("Su reserva venció y los asientos se liberaron. Elíjalos de nuevo.");
            }
            validarCantidad(asientos.size());
            long idVenta = ventaDAO.insertarVenta(cn, idCliente, "Web", null, total(asientos), formaPago);
            for (AsientoSeleccionado a : asientos) {
                ventaDAO.insertarEntrada(cn, idVenta, a.getIdEventoAsiento(), a.getPrecio(), GeneradorCodigo.nuevo());
                ventaDAO.marcarVendido(cn, a.getIdEventoAsiento());
            }
            actualizarAgotado(cn, evento);
            return idVenta;
        }, "No se pudo completar la compra");
    }

    /** Venta en mostrador (RF-24): el Administrador vende a nombre de un cliente. */
    public long venderMostrador(long idEvento, long idCliente, List<Long> idsAsientos, String formaPago, long idVendedor) {
        List<Long> ids = sinRepetidos(idsAsientos);
        validarCantidad(ids.size());
        validarFormaPago(formaPago);
        return enTransaccion(cn -> {
            Evento evento = eventoAbierto(cn, idEvento);
            List<Long> fallidos = new ArrayList<>();
            for (Long id : ids) {
                if (!ventaDAO.venderDirecto(cn, idEvento, id)) {
                    fallidos.add(id);
                }
            }
            if (!fallidos.isEmpty()) {
                throw asientosTomados(cn, idEvento, fallidos);
            }
            List<AsientoSeleccionado> asientos = ventaDAO.asientosPorIds(cn, idEvento, ids);
            long idVenta = ventaDAO.insertarVenta(cn, idCliente, "Mostrador", idVendedor, total(asientos), formaPago);
            for (AsientoSeleccionado a : asientos) {
                ventaDAO.insertarEntrada(cn, idVenta, a.getIdEventoAsiento(), a.getPrecio(), GeneradorCodigo.nuevo());
            }
            actualizarAgotado(cn, evento);
            return idVenta;
        }, "No se pudo registrar la venta en mostrador");
    }

    /** Anulación de una compra propia (RF-23, RN-12, RN-13). */
    public void anular(long idVenta, long idCliente, String motivo) {
        String m = motivo == null ? "" : motivo.trim();
        if (m.length() < 5 || m.length() > 200) {
            throw new ServicioException("Escriba el motivo de la anulación (de 5 a 200 caracteres).");
        }
        enTransaccion(cn -> {
            VentaAnulable v = ventaDAO.ventaParaAnular(cn, idVenta)
                    .filter(x -> x.getIdCliente() == idCliente)
                    .orElseThrow(() -> new ServicioException("La compra no existe."));
            String error = ReglasCompra.motivoNoAnulable(v, LocalDateTime.now(), Configuracion.horasAnulacion());
            if (error != null) {
                throw new ServicioException(error);
            }
            Evento evento = eventoDAO.buscarParaActualizar(cn, v.getIdEvento())
                    .orElseThrow(() -> new ServicioException("El evento no existe."));
            ventaDAO.anularVenta(cn, idVenta, m);
            actualizarAgotado(cn, evento);
            return null;
        }, "No se pudo anular la compra");
    }

    /** RN-11: Agotado si ya no quedan asientos disponibles ni reservados; si se liberan, vuelve a Publicado. */
    private void actualizarAgotado(Connection cn, Evento evento) {
        int pendientes = ventaDAO.contarPendientes(cn, evento.getId());
        if (pendientes == 0 && evento.getEstado() == EstadoEvento.PUBLICADO) {
            eventoDAO.cambiarEstado(cn, evento.getId(), EstadoEvento.AGOTADO);
        } else if (pendientes > 0 && evento.getEstado() == EstadoEvento.AGOTADO) {
            eventoDAO.cambiarEstado(cn, evento.getId(), EstadoEvento.PUBLICADO);
        }
    }

    private static void validarFormaPago(String formaPago) {
        if (formaPago == null || !FORMAS_PAGO.contains(formaPago)) {
            throw new ServicioException("Seleccione la forma de pago.");
        }
    }

    private static BigDecimal total(List<AsientoSeleccionado> asientos) {
        return asientos.stream().map(AsientoSeleccionado::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
