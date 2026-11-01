package sv.edu.itca.ticka.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.IncidenciaDAO;
import sv.edu.itca.ticka.modelo.Incidencia;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de IncidenciaDAO. */
public class IncidenciaDAOImpl implements IncidenciaDAO {

    private static final String SQL_SELECT = "SELECT i.id, i.id_evento, ev.nombre AS evento, i.tipo, i.descripcion, "
            + "i.gravedad, i.puerta, i.estado, i.observaciones, i.fecha_hora, i.id_usuario, u.nombre AS usuario "
            + "FROM incidencia i JOIN evento ev ON ev.id = i.id_evento JOIN usuario u ON u.id = i.id_usuario ";

    private static Incidencia mapear(ResultSet rs) throws SQLException {
        Incidencia i = new Incidencia();
        i.setId(rs.getLong("id"));
        i.setIdEvento(rs.getLong("id_evento"));
        i.setEvento(rs.getString("evento"));
        i.setTipo(rs.getString("tipo"));
        i.setDescripcion(rs.getString("descripcion"));
        i.setGravedad(rs.getString("gravedad"));
        i.setPuerta(rs.getString("puerta"));
        i.setEstado(rs.getString("estado"));
        i.setObservaciones(rs.getString("observaciones"));
        i.setFechaHora(rs.getObject("fecha_hora", LocalDateTime.class));
        i.setIdUsuario(rs.getLong("id_usuario"));
        i.setUsuario(rs.getString("usuario"));
        return i;
    }

    @Override
    public long insertar(Incidencia i) {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return insertar(cn, i);
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar la incidencia", ex);
        }
    }

    @Override
    public long insertar(Connection cn, Incidencia i) {
        String sql = "INSERT INTO incidencia (id_evento, tipo, descripcion, gravedad, puerta, estado, id_usuario) "
                + "VALUES (?, ?, ?, ?, ?, 'Abierta', ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, i.getIdEvento());
            ps.setString(2, i.getTipo());
            ps.setString(3, i.getDescripcion());
            ps.setString(4, i.getGravedad());
            ps.setString(5, i.getPuerta());
            ps.setLong(6, i.getIdUsuario());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                long id = rs.getLong(1);
                i.setId(id);
                return id;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar la incidencia", ex);
        }
    }

    @Override
    public List<Incidencia> listar(Long idEvento, String tipo, String gravedad, String estado, int limite) {
        String sql = SQL_SELECT + "WHERE (? IS NULL OR i.id_evento = ?) AND (? IS NULL OR i.tipo = ?) "
                + "AND (? IS NULL OR i.gravedad = ?) AND (? IS NULL OR i.estado = ?) "
                + "ORDER BY i.fecha_hora DESC, i.id DESC LIMIT ?";
        List<Incidencia> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEvento);
            ps.setObject(2, idEvento);
            ps.setString(3, tipo);
            ps.setString(4, tipo);
            ps.setString(5, gravedad);
            ps.setString(6, gravedad);
            ps.setString(7, estado);
            ps.setString(8, estado);
            ps.setInt(9, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar las incidencias", ex);
        }
    }

    @Override
    public Optional<Incidencia> buscarPorId(long id) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_SELECT + "WHERE i.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la incidencia", ex);
        }
    }

    @Override
    public void actualizarEstado(long id, String estado, String observaciones) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE incidencia SET estado = ?, observaciones = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setString(2, observaciones);
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar la incidencia", ex);
        }
    }
}
