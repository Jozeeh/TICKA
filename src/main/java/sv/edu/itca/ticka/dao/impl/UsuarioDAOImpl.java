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
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/**
 * Implementación JDBC de UsuarioDAO. Todo el SQL es parametrizado
 * (PreparedStatement) para evitar inyección SQL.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SQL_SELECT
            = "SELECT u.id, u.username, u.password_hash, u.nombre, r.nombre AS rol, u.activo, "
            + "u.intentos_fallidos, u.bloqueado_hasta, u.creado_en "
            + "FROM usuario u JOIN rol r ON r.id = u.id_rol ";

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNombre(rs.getString("nombre"));
        u.setRol(Rol.desdeNombre(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setBloqueadoHasta(rs.getObject("bloqueado_hasta", LocalDateTime.class));
        u.setCreadoEn(rs.getObject("creado_en", LocalDateTime.class));
        return u;
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return buscarUno(SQL_SELECT + "WHERE u.username = ?", username);
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return buscarUno(SQL_SELECT + "WHERE u.id = ?", id);
    }

    private Optional<Usuario> buscarUno(String sql, Object parametro) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el usuario", ex);
        }
    }

    @Override
    public List<Usuario> listarPersonal() {
        String sql = SQL_SELECT + "WHERE r.nombre <> 'Cliente' ORDER BY u.activo DESC, r.nombre, u.nombre";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar el personal", ex);
        }
    }

    @Override
    public boolean existeAdministradorActivo() {
        return contarAdministradoresActivos() > 0;
    }

    @Override
    public int contarAdministradoresActivos() {
        String sql = "SELECT COUNT(*) FROM usuario u JOIN rol r ON r.id = u.id_rol "
                + "WHERE r.nombre = 'Administrador' AND u.activo = 1";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar los administradores", ex);
        }
    }

    @Override
    public boolean existeUsername(String username, Long excluirId) {
        String sql = "SELECT 1 FROM usuario WHERE username = ? AND (? IS NULL OR id <> ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setObject(2, excluirId);
            ps.setObject(3, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar el nombre de usuario", ex);
        }
    }

    @Override
    public long insertar(Usuario u) {
        String sql = "INSERT INTO usuario (username, password_hash, nombre, id_rol, activo) "
                + "VALUES (?, ?, ?, (SELECT id FROM rol WHERE nombre = ?), ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getNombre());
            ps.setString(4, u.getRol().getNombre());
            ps.setBoolean(5, u.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                long id = rs.getLong(1);
                u.setId(id);
                return id;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar el usuario", ex);
        }
    }

    @Override
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE usuario SET nombre = ?, id_rol = (SELECT id FROM rol WHERE nombre = ?), "
                + "activo = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRol().getNombre());
            ps.setBoolean(3, u.isActivo());
            ps.setLong(4, u.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el usuario", ex);
        }
    }

    @Override
    public boolean cambiarClave(long id, String passwordHash) {
        return actualizarCampo("UPDATE usuario SET password_hash = ?, intentos_fallidos = 0, "
                + "bloqueado_hasta = NULL WHERE id = ?", passwordHash, id);
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        return actualizarCampo("UPDATE usuario SET activo = ? WHERE id = ?", activo, id);
    }

    private boolean actualizarCampo(String sql, Object valor, long id) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el usuario", ex);
        }
    }

    @Override
    public void registrarIntentoFallido(long id, int intentos, LocalDateTime bloqueadoHasta) {
        String sql = "UPDATE usuario SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, intentos);
            ps.setObject(2, bloqueadoHasta);
            ps.setLong(3, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar el intento fallido", ex);
        }
    }

    @Override
    public void reiniciarIntentos(long id) {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo reiniciar los intentos", ex);
        }
    }
}
