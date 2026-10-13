package sv.edu.itca.ticka.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.modelo.Recinto;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de RecintoDAO. */
public class RecintoDAOImpl implements RecintoDAO {

    private static final String SQL_SELECT
            = "SELECT r.id, r.nombre, r.direccion, r.tipo, r.activo, "
            + "(SELECT COUNT(*) FROM zona z WHERE z.id_recinto = r.id) AS total_zonas, "
            + "(SELECT COUNT(*) FROM asiento a JOIN zona z ON z.id = a.id_zona "
            + " WHERE z.id_recinto = r.id AND a.activo = 1) AS total_asientos "
            + "FROM recinto r ";

    private Recinto mapear(ResultSet rs) throws SQLException {
        Recinto r = new Recinto();
        r.setId(rs.getLong("id"));
        r.setNombre(rs.getString("nombre"));
        r.setDireccion(rs.getString("direccion"));
        r.setTipo(rs.getString("tipo"));
        r.setActivo(rs.getBoolean("activo"));
        r.setTotalZonas(rs.getInt("total_zonas"));
        r.setTotalAsientos(rs.getInt("total_asientos"));
        return r;
    }

    @Override
    public List<Recinto> listar() {
        return consultar(SQL_SELECT + "ORDER BY r.activo DESC, r.nombre");
    }

    @Override
    public List<Recinto> listarActivos() {
        return consultar(SQL_SELECT + "WHERE r.activo = 1 ORDER BY r.nombre");
    }

    private List<Recinto> consultar(String sql) {
        List<Recinto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los recintos", ex);
        }
    }

    @Override
    public Optional<Recinto> buscarPorId(long id) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_SELECT + "WHERE r.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el recinto", ex);
        }
    }

    @Override
    public boolean existeNombre(String nombre, Long excluirId) {
        String sql = "SELECT 1 FROM recinto WHERE nombre = ? AND (? IS NULL OR id <> ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setObject(2, excluirId);
            ps.setObject(3, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar el recinto", ex);
        }
    }

    @Override
    public long insertar(Recinto r) {
        String sql = "INSERT INTO recinto (nombre, direccion, tipo, activo) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNombre());
            ps.setString(2, r.getDireccion());
            ps.setString(3, r.getTipo());
            ps.setBoolean(4, r.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar el recinto", ex);
        }
    }

    @Override
    public boolean actualizar(Recinto r) {
        String sql = "UPDATE recinto SET nombre = ?, direccion = ?, tipo = ?, activo = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setString(2, r.getDireccion());
            ps.setString(3, r.getTipo());
            ps.setBoolean(4, r.isActivo());
            ps.setLong(5, r.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el recinto", ex);
        }
    }
}
