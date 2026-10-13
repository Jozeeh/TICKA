package sv.edu.itca.ticka.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de ClienteDAO. */
public class ClienteDAOImpl implements ClienteDAO {

    @Override
    public List<Cliente> listar(String texto) {
        String sql = "SELECT c.id, c.id_usuario, u.username, c.nombres, c.apellidos, c.documento, c.correo, "
                + "c.telefono, u.activo, "
                + "(SELECT COUNT(*) FROM venta v WHERE v.id_cliente = c.id AND v.estado = 'Pagada') AS compras, "
                + "(SELECT COUNT(*) FROM entrada en JOIN venta v ON v.id = en.id_venta "
                + " WHERE v.id_cliente = c.id AND en.estado <> 'Anulada') AS boletos, "
                + "(SELECT COALESCE(SUM(v.total), 0) FROM venta v WHERE v.id_cliente = c.id "
                + " AND v.estado = 'Pagada') AS total_gastado "
                + "FROM cliente c JOIN usuario u ON u.id = c.id_usuario "
                + "WHERE (? IS NULL OR CONCAT_WS(' ', c.nombres, c.apellidos, c.documento, c.correo, u.username) LIKE ?) "
                + "ORDER BY c.apellidos, c.nombres";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, texto);
            ps.setString(2, texto == null ? null : "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    lista.add(c);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los clientes", ex);
        }
    }
}
