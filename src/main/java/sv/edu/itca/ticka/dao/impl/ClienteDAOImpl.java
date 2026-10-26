package sv.edu.itca.ticka.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.CompraCliente;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de ClienteDAO. */
public class ClienteDAOImpl implements ClienteDAO {

    private static final String SQL_SELECT
            = "SELECT c.id, c.id_usuario, u.username, c.nombres, c.apellidos, c.documento, c.correo, "
            + "c.telefono, u.activo, "
            + "(SELECT COUNT(*) FROM venta v WHERE v.id_cliente = c.id AND v.estado = 'Pagada') AS compras, "
            + "(SELECT COUNT(*) FROM entrada en JOIN venta v ON v.id = en.id_venta "
            + " WHERE v.id_cliente = c.id AND en.estado <> 'Anulada') AS boletos, "
            + "(SELECT COALESCE(SUM(v.total), 0) FROM venta v WHERE v.id_cliente = c.id "
            + " AND v.estado = 'Pagada') AS total_gastado "
            + "FROM cliente c JOIN usuario u ON u.id = c.id_usuario ";

    /** Búsqueda por nombre, documento, correo o usuario (2 parámetros). */
    private static final String SQL_BUSQUEDA
            = "WHERE (? IS NULL OR CONCAT_WS(' ', c.nombres, c.apellidos, c.documento, c.correo, u.username) LIKE ?) ";

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getLong("id"));
        c.setIdUsuario(rs.getLong("id_usuario"));
        c.setUsername(rs.getString("username"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setDocumento(rs.getString("documento"));
        c.setCorreo(rs.getString("correo"));
        c.setTelefono(rs.getString("telefono"));
        c.setActivo(rs.getBoolean("activo"));
        c.setCompras(rs.getInt("compras"));
        c.setBoletos(rs.getInt("boletos"));
        c.setTotalGastado(rs.getBigDecimal("total_gastado"));
        return c;
    }

    private static void asignarBusqueda(PreparedStatement ps, String texto) throws SQLException {
        ps.setString(1, texto);
        ps.setString(2, texto == null ? null : "%" + texto + "%");
    }

    @Override
    public List<Cliente> listar(String texto, int desplazamiento, int limite) {
        String sql = SQL_SELECT + SQL_BUSQUEDA + "ORDER BY c.apellidos, c.nombres LIMIT ? OFFSET ?";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarBusqueda(ps, texto);
            ps.setInt(3, limite);
            ps.setInt(4, desplazamiento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los clientes", ex);
        }
    }

    @Override
    public int contar(String texto) {
        String sql = "SELECT COUNT(*) FROM cliente c JOIN usuario u ON u.id = c.id_usuario " + SQL_BUSQUEDA;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarBusqueda(ps, texto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo contar los clientes", ex);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(long id) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_SELECT + "WHERE c.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el cliente", ex);
        }
    }

    @Override
    public List<CompraCliente> compras(long idCliente) {
        String sql = "SELECT v.id, v.fecha, v.canal, v.forma_pago, v.estado AS estado_venta, v.total, "
                + "v.motivo_anulacion, ev.nombre AS evento, ev.fecha_inicio, z.nombre AS zona, a.fila, a.numero, "
                + "en.precio, en.estado AS estado_boleto, en.codigo "
                + "FROM venta v "
                + "JOIN entrada en ON en.id_venta = v.id "
                + "JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "JOIN evento ev ON ev.id = ea.id_evento "
                + "JOIN asiento a ON a.id = ea.id_asiento "
                + "JOIN zona z ON z.id = a.id_zona "
                + "WHERE v.id_cliente = ? "
                + "ORDER BY v.fecha DESC, v.id DESC, z.id, a.fila, a.numero";
        // Las filas llegan una por boleto; se agrupan por venta conservando el orden.
        Map<Long, CompraCliente> compras = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long idVenta = rs.getLong("id");
                    CompraCliente compra = compras.get(idVenta);
                    if (compra == null) {
                        compra = new CompraCliente();
                        compra.setId(idVenta);
                        compra.setFecha(rs.getObject("fecha", LocalDateTime.class));
                        compra.setCanal(rs.getString("canal"));
                        compra.setFormaPago(rs.getString("forma_pago"));
                        compra.setEstado(rs.getString("estado_venta"));
                        compra.setTotal(rs.getBigDecimal("total"));
                        compra.setMotivoAnulacion(rs.getString("motivo_anulacion"));
                        compras.put(idVenta, compra);
                    }
                    CompraCliente.Boleto b = new CompraCliente.Boleto();
                    b.setEvento(rs.getString("evento"));
                    b.setFechaEvento(rs.getObject("fecha_inicio", LocalDateTime.class));
                    b.setZona(rs.getString("zona"));
                    b.setFila(rs.getString("fila"));
                    b.setNumero(rs.getInt("numero"));
                    b.setPrecio(rs.getBigDecimal("precio"));
                    b.setEstado(rs.getString("estado_boleto"));
                    b.setCodigo(rs.getString("codigo"));
                    compra.getBoletos().add(b);
                }
            }
            return new ArrayList<>(compras.values());
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar las compras del cliente", ex);
        }
    }
}
