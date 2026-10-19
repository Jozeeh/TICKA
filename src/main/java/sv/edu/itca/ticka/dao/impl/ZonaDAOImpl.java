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
import sv.edu.itca.ticka.dao.ZonaDAO;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.Zona;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de ZonaDAO. */
public class ZonaDAOImpl implements ZonaDAO {

    private static final String SQL_SELECT
            = "SELECT z.id, z.id_recinto, z.nombre, z.descripcion, "
            + "(SELECT COUNT(*) FROM asiento a WHERE a.id_zona = z.id) AS total_asientos, "
            + "(SELECT COUNT(*) FROM asiento a WHERE a.id_zona = z.id AND a.activo = 1) AS asientos_activos "
            + "FROM zona z ";

    private Zona mapear(ResultSet rs) throws SQLException {
        Zona z = new Zona();
        z.setId(rs.getLong("id"));
        z.setIdRecinto(rs.getLong("id_recinto"));
        z.setNombre(rs.getString("nombre"));
        z.setDescripcion(rs.getString("descripcion"));
        z.setTotalAsientos(rs.getInt("total_asientos"));
        z.setAsientosActivos(rs.getInt("asientos_activos"));
        return z;
    }

    @Override
    public List<Zona> listarPorRecinto(long idRecinto) {
        List<Zona> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_SELECT + "WHERE z.id_recinto = ? ORDER BY z.id")) {
            ps.setLong(1, idRecinto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar las zonas", ex);
        }
    }

    @Override
    public Optional<Zona> buscarPorId(long id) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_SELECT + "WHERE z.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la zona", ex);
        }
    }

    @Override
    public boolean existeNombre(long idRecinto, String nombre, Long excluirId) {
        String sql = "SELECT 1 FROM zona WHERE id_recinto = ? AND nombre = ? AND (? IS NULL OR id <> ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idRecinto);
            ps.setString(2, nombre);
            ps.setObject(3, excluirId);
            ps.setObject(4, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar la zona", ex);
        }
    }

    @Override
    public long insertar(Zona z) {
        String sql = "INSERT INTO zona (id_recinto, nombre, descripcion) VALUES (?, ?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, z.getIdRecinto());
            ps.setString(2, z.getNombre());
            ps.setString(3, z.getDescripcion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar la zona", ex);
        }
    }

    @Override
    public boolean actualizar(Zona z) {
        String sql = "UPDATE zona SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, z.getNombre());
            ps.setString(2, z.getDescripcion());
            ps.setLong(3, z.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar la zona", ex);
        }
    }

    @Override
    public boolean eliminar(long id) {
        String sql = "DELETE FROM zona WHERE id = ? AND NOT EXISTS (SELECT 1 FROM asiento WHERE id_zona = ?) "
                + "AND NOT EXISTS (SELECT 1 FROM precio_zona WHERE id_zona = ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo eliminar la zona", ex);
        }
    }

    @Override
    public List<Asiento> listarAsientosPorRecinto(long idRecinto) {
        String sql = "SELECT a.id, a.id_zona, a.fila, a.numero, a.activo "
                + "FROM asiento a JOIN zona z ON z.id = a.id_zona "
                + "WHERE z.id_recinto = ? ORDER BY z.id, LENGTH(a.fila), a.fila, a.numero";
        List<Asiento> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idRecinto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asiento a = new Asiento();
                    a.setId(rs.getLong("id"));
                    a.setIdZona(rs.getLong("id_zona"));
                    a.setFila(rs.getString("fila"));
                    a.setNumero(rs.getInt("numero"));
                    a.setActivo(rs.getBoolean("activo"));
                    lista.add(a);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los asientos", ex);
        }
    }

    @Override
    public int[] configurarAsientos(long idZona, List<String> filas, int porFila) {
        // INSERT IGNORE omite los asientos que ya existen (UNIQUE zona+fila+número).
        String sqlCrear = "INSERT IGNORE INTO asiento (id_zona, fila, numero) VALUES (?, ?, ?)";
        String enFilas = String.join(",", java.util.Collections.nCopies(filas.size(), "?"));
        String sqlActivar = "UPDATE asiento SET activo = 1 WHERE id_zona = ? AND activo = 0 "
                + "AND fila IN (" + enFilas + ") AND numero <= ?";
        String sqlInactivar = "UPDATE asiento SET activo = 0 WHERE id_zona = ? AND activo = 1 "
                + "AND (fila NOT IN (" + enFilas + ") OR numero > ?)";
        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement crear = cn.prepareStatement(sqlCrear);
                    PreparedStatement activar = cn.prepareStatement(sqlActivar);
                    PreparedStatement inactivar = cn.prepareStatement(sqlInactivar)) {
                for (String fila : filas) {
                    for (int numero = 1; numero <= porFila; numero++) {
                        crear.setLong(1, idZona);
                        crear.setString(2, fila);
                        crear.setInt(3, numero);
                        crear.addBatch();
                    }
                }
                int creados = 0;
                for (int r : crear.executeBatch()) {
                    if (r > 0) {
                        creados += r;
                    }
                }
                int activados = ejecutarConFilas(activar, idZona, filas, porFila);
                int inactivados = ejecutarConFilas(inactivar, idZona, filas, porFila);
                cn.commit();
                return new int[]{creados, activados, inactivados};
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar los asientos de la zona", ex);
        }
    }

    /** Parámetros: zona, cada letra de fila y el número máximo de asiento. */
    private static int ejecutarConFilas(PreparedStatement ps, long idZona, List<String> filas, int porFila)
            throws SQLException {
        int i = 1;
        ps.setLong(i++, idZona);
        for (String fila : filas) {
            ps.setString(i++, fila);
        }
        ps.setInt(i, porFila);
        return ps.executeUpdate();
    }

    @Override
    public boolean cambiarActivoAsiento(long idAsiento, boolean activo) {
        String sql = "UPDATE asiento SET activo = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setLong(2, idAsiento);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el asiento", ex);
        }
    }
}
