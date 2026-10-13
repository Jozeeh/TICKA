package sv.edu.itca.ticka.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.dao.CategoriaDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.modelo.Categoria;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de CategoriaDAO. */
public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public List<Categoria> listar() {
        String sql = "SELECT c.id, c.nombre, "
                + "(SELECT COUNT(*) FROM evento e WHERE e.id_categoria = c.id) AS total_eventos "
                + "FROM categoria c ORDER BY c.nombre";
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria(rs.getLong("id"), rs.getString("nombre"));
                c.setTotalEventos(rs.getInt("total_eventos"));
                lista.add(c);
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar las categorías", ex);
        }
    }

    @Override
    public Optional<Categoria> buscarPorId(long id) {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new Categoria(rs.getLong("id"), rs.getString("nombre")))
                        : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la categoría", ex);
        }
    }

    @Override
    public boolean existeNombre(String nombre, Long excluirId) {
        String sql = "SELECT 1 FROM categoria WHERE nombre = ? AND (? IS NULL OR id <> ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setObject(2, excluirId);
            ps.setObject(3, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar la categoría", ex);
        }
    }

    @Override
    public long insertar(Categoria c) {
        String sql = "INSERT INTO categoria (nombre) VALUES (?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar la categoría", ex);
        }
    }

    @Override
    public boolean actualizar(Categoria c) {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setLong(2, c.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar la categoría", ex);
        }
    }

    @Override
    public boolean eliminar(long id) {
        String sql = "DELETE FROM categoria WHERE id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM evento WHERE id_categoria = ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo eliminar la categoría", ex);
        }
    }
}
