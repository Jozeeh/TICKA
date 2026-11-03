package sv.edu.itca.ticka.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.VentaDAO;
import sv.edu.itca.ticka.modelo.AsientoSeleccionado;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.VentaAnulable;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de VentaDAO. */
public class VentaDAOImpl implements VentaDAO {

    /** Asiento del evento con zona, fila, número, precio de la zona y fin de la reserva. */
    private static final String SQL_ASIENTO = "SELECT ea.id, z.nombre AS zona, a.fila, a.numero, pz.precio, "
            + "ea.reservado_hasta FROM evento_asiento ea "
            + "JOIN asiento a ON a.id = ea.id_asiento "
            + "JOIN zona z ON z.id = a.id_zona "
            + "JOIN precio_zona pz ON pz.id_evento = ea.id_evento AND pz.id_zona = a.id_zona ";

    private static final String SQL_RESERVADOS = SQL_ASIENTO
            + "WHERE ea.id_evento = ? AND ea.estado = 'Reservado' AND ea.id_cliente_reserva = ? "
            + "AND ea.reservado_hasta >= NOW() ORDER BY z.id, LENGTH(a.fila), a.fila, a.numero";

    private static AsientoSeleccionado mapear(ResultSet rs) throws SQLException {
        AsientoSeleccionado a = new AsientoSeleccionado();
        a.setIdEventoAsiento(rs.getLong("id"));
        a.setZona(rs.getString("zona"));
        a.setFila(rs.getString("fila"));
        a.setNumero(rs.getInt("numero"));
        a.setPrecio(rs.getBigDecimal("precio"));
        a.setReservadoHasta(rs.getObject("reservado_hasta", LocalDateTime.class));
        return a;
    }

    private static List<AsientoSeleccionado> leer(PreparedStatement ps) throws SQLException {
        List<AsientoSeleccionado> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public boolean reservar(Connection cn, long idEvento, long idEventoAsiento, long idCliente, LocalDateTime hasta) {
        String sql = "UPDATE evento_asiento SET estado = 'Reservado', reservado_hasta = ?, id_cliente_reserva = ? "
                + "WHERE id = ? AND id_evento = ? AND (estado = 'Disponible' "
                + "OR (estado = 'Reservado' AND (reservado_hasta < NOW() OR id_cliente_reserva = ?)))";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, hasta);
            ps.setLong(2, idCliente);
            ps.setLong(3, idEventoAsiento);
            ps.setLong(4, idEvento);
            ps.setLong(5, idCliente);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo reservar el asiento", ex);
        }
    }

    @Override
    public int liberarReservas(Connection cn, long idEvento, long idCliente) {
        String sql = "UPDATE evento_asiento SET estado = 'Disponible', reservado_hasta = NULL, id_cliente_reserva = NULL "
                + "WHERE id_evento = ? AND estado = 'Reservado' AND id_cliente_reserva = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            ps.setLong(2, idCliente);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo liberar la reserva", ex);
        }
    }

    @Override
    public List<AsientoSeleccionado> reservados(long idEvento, long idCliente) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_RESERVADOS)) {
            ps.setLong(1, idEvento);
            ps.setLong(2, idCliente);
            return leer(ps);
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la reserva", ex);
        }
    }

    @Override
    public List<AsientoSeleccionado> reservadosParaComprar(Connection cn, long idEvento, long idCliente) {
        try (PreparedStatement ps = cn.prepareStatement(SQL_RESERVADOS + " FOR UPDATE")) {
            ps.setLong(1, idEvento);
            ps.setLong(2, idCliente);
            return leer(ps);
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la reserva", ex);
        }
    }

    @Override
    public List<AsientoSeleccionado> asientosPorIds(Connection cn, long idEvento, List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        String marcas = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = SQL_ASIENTO + "WHERE ea.id_evento = ? AND ea.id IN (" + marcas + ") "
                + "ORDER BY z.id, LENGTH(a.fila), a.fila, a.numero";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 2, ids.get(i));
            }
            return leer(ps);
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar los asientos", ex);
        }
    }

    @Override
    public boolean venderDirecto(Connection cn, long idEvento, long idEventoAsiento) {
        String sql = "UPDATE evento_asiento SET estado = 'Vendido', reservado_hasta = NULL, id_cliente_reserva = NULL "
                + "WHERE id = ? AND id_evento = ? "
                + "AND (estado = 'Disponible' OR (estado = 'Reservado' AND reservado_hasta < NOW()))";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEventoAsiento);
            ps.setLong(2, idEvento);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo vender el asiento", ex);
        }
    }

    @Override
    public void marcarVendido(Connection cn, long idEventoAsiento) {
        String sql = "UPDATE evento_asiento SET estado = 'Vendido', reservado_hasta = NULL, id_cliente_reserva = NULL "
                + "WHERE id = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEventoAsiento);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo marcar el asiento como vendido", ex);
        }
    }

    @Override
    public long insertarVenta(Connection cn, long idCliente, String canal, Long idVendedor, BigDecimal total,
            String formaPago) {
        String sql = "INSERT INTO venta (id_cliente, canal, id_usuario_vendedor, total, forma_pago, estado) "
                + "VALUES (?, ?, ?, ?, ?, 'Pagada')";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, idCliente);
            ps.setString(2, canal);
            ps.setObject(3, idVendedor);
            ps.setBigDecimal(4, total);
            ps.setString(5, formaPago);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar la venta", ex);
        }
    }

    @Override
    public boolean insertarEntrada(Connection cn, long idVenta, long idEventoAsiento, BigDecimal precio, String codigo) {
        String sql = "INSERT INTO entrada (id_venta, id_evento_asiento, precio, codigo, estado) VALUES (?, ?, ?, ?, 'Emitida')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idVenta);
            ps.setLong(2, idEventoAsiento);
            ps.setBigDecimal(3, precio);
            ps.setString(4, codigo);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException ex) {
            // 1062 = clave duplicada en uq_entrada_codigo: el código ya existe.
            if (ex.getErrorCode() == 1062 && String.valueOf(ex.getMessage()).contains("uq_entrada_codigo")) {
                return false;
            }
            throw new DAOException("No se pudo emitir el boleto", ex);
        } catch (SQLException ex) {
            throw new DAOException("No se pudo emitir el boleto", ex);
        }
    }

    @Override
    public int contarPendientes(Connection cn, long idEvento) {
        String sql = "SELECT COUNT(*) FROM evento_asiento WHERE id_evento = ? AND estado IN ('Disponible', 'Reservado')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo contar los asientos pendientes", ex);
        }
    }

    @Override
    public Optional<VentaAnulable> ventaParaAnular(Connection cn, long idVenta) {
        String sqlBloqueo = "SELECT id FROM venta WHERE id = ? FOR UPDATE";
        String sql = "SELECT v.id, v.id_cliente, v.estado, ev.id AS id_evento, ev.estado AS estado_evento, "
                + "ev.apertura_puertas, SUM(en.estado = 'Usada') AS usados "
                + "FROM venta v JOIN entrada en ON en.id_venta = v.id "
                + "JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "JOIN evento ev ON ev.id = ea.id_evento "
                + "WHERE v.id = ? GROUP BY v.id, v.id_cliente, v.estado, ev.id, ev.estado, ev.apertura_puertas";
        try (PreparedStatement psBloqueo = cn.prepareStatement(sqlBloqueo);
                PreparedStatement ps = cn.prepareStatement(sql)) {
            psBloqueo.setLong(1, idVenta);
            try (ResultSet rs = psBloqueo.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
            }
            ps.setLong(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                VentaAnulable v = new VentaAnulable();
                v.setId(rs.getLong("id"));
                v.setIdCliente(rs.getLong("id_cliente"));
                v.setEstado(rs.getString("estado"));
                v.setIdEvento(rs.getLong("id_evento"));
                v.setEstadoEvento(EstadoEvento.desdeBd(rs.getString("estado_evento")));
                v.setAperturaPuertas(rs.getObject("apertura_puertas", LocalDateTime.class));
                v.setBoletosUsados(rs.getInt("usados"));
                return Optional.of(v);
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la venta", ex);
        }
    }

    @Override
    public void anularVenta(Connection cn, long idVenta, String motivo) {
        // Primero se liberan los asientos (mientras los boletos siguen Emitidos), luego se anulan.
        String sqlAsientos = "UPDATE evento_asiento ea JOIN entrada en ON en.id_evento_asiento = ea.id "
                + "SET ea.estado = 'Disponible', ea.reservado_hasta = NULL, ea.id_cliente_reserva = NULL "
                + "WHERE en.id_venta = ? AND en.estado = 'Emitida'";
        String sqlBoletos = "UPDATE entrada SET estado = 'Anulada' WHERE id_venta = ? AND estado = 'Emitida'";
        String sqlVenta = "UPDATE venta SET estado = 'Anulada', motivo_anulacion = ? WHERE id = ? AND estado = 'Pagada'";
        try (PreparedStatement psAsientos = cn.prepareStatement(sqlAsientos);
                PreparedStatement psBoletos = cn.prepareStatement(sqlBoletos);
                PreparedStatement psVenta = cn.prepareStatement(sqlVenta)) {
            psAsientos.setLong(1, idVenta);
            psAsientos.executeUpdate();
            psBoletos.setLong(1, idVenta);
            psBoletos.executeUpdate();
            psVenta.setString(1, motivo);
            psVenta.setLong(2, idVenta);
            psVenta.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo anular la venta", ex);
        }
    }

    @Override
    public Optional<String> codigoDeBoleto(long idEntrada, long idCliente) {
        String sql = "SELECT en.codigo FROM entrada en JOIN venta v ON v.id = en.id_venta "
                + "WHERE en.id = ? AND v.id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEntrada);
            ps.setLong(2, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el boleto", ex);
        }
    }
}
