package sv.edu.itca.ticka.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.AsientoSeleccionado;
import sv.edu.itca.ticka.modelo.VentaAnulable;

/**
 * DAO de reservas, ventas y boletos (M3). Los métodos que reciben una
 * Connection forman parte de una transacción abierta por CompraServicio.
 *
 * Concurrencia (RNF-05): reservar y vender usan una actualización
 * condicional; si no cambia la fila, otro cliente ya tomó el asiento.
 */
public interface VentaDAO {

    /**
     * Reserva un asiento para el cliente hasta la hora indicada (RF-18). Solo
     * cambia si está Disponible, con una reserva vencida o ya reservado por
     * el mismo cliente. Devuelve false si otro lo tomó.
     */
    boolean reservar(Connection cn, long idEvento, long idEventoAsiento, long idCliente, LocalDateTime hasta);

    /** Libera las reservas del cliente en el evento. */
    int liberarReservas(Connection cn, long idEvento, long idCliente);

    /** Asientos reservados (vigentes) por el cliente en el evento, con su precio. */
    List<AsientoSeleccionado> reservados(long idEvento, long idCliente);

    /** Igual que reservados(), bloqueando las filas para comprar (FOR UPDATE). */
    List<AsientoSeleccionado> reservadosParaComprar(Connection cn, long idEvento, long idCliente);

    /** Zona, fila, número y precio de los asientos indicados del evento. */
    List<AsientoSeleccionado> asientosPorIds(Connection cn, long idEvento, List<Long> ids);

    /** Venta directa en mostrador: pasa a Vendido solo si está libre. */
    boolean venderDirecto(Connection cn, long idEvento, long idEventoAsiento);

    /** Pasa a Vendido un asiento ya reservado por el comprador. */
    void marcarVendido(Connection cn, long idEventoAsiento);

    long insertarVenta(Connection cn, long idCliente, String canal, Long idVendedor, BigDecimal total, String formaPago);

    void insertarEntrada(Connection cn, long idVenta, long idEventoAsiento, BigDecimal precio, String codigo);

    /** Asientos que todavía se pueden vender (Disponibles o Reservados), para RN-11. */
    int contarPendientes(Connection cn, long idEvento);

    /** Bloquea la venta y devuelve lo necesario para validar la anulación (RN-12). */
    Optional<VentaAnulable> ventaParaAnular(Connection cn, long idVenta);

    /** Anula la venta, sus boletos emitidos y libera los asientos (RF-23). */
    void anularVenta(Connection cn, long idVenta, String motivo);

    /** Código del boleto solo si pertenece al cliente (RN-13). */
    Optional<String> codigoDeBoleto(long idEntrada, long idCliente);
}
