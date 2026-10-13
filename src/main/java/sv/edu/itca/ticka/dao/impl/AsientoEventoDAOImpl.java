package sv.edu.itca.ticka.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sv.edu.itca.ticka.dao.AsientoEventoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.OcupacionZona;
import sv.edu.itca.ticka.modelo.PrecioZona;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de AsientoEventoDAO. */
public class AsientoEventoDAOImpl implements AsientoEventoDAO {

    @Override
    public List<PrecioZona> listarPrecios(long idEvento, long idRecinto) {
        String sql = "SELECT z.id, z.nombre, pz.precio, "
                + "(SELECT COUNT(*) FROM asiento a WHERE a.id_zona = z.id AND a.activo = 1) AS activos "
                + "FROM zona z LEFT JOIN precio_zona pz ON pz.id_zona = z.id AND pz.id_evento = ? "
                + "WHERE z.id_recinto = ? ORDER BY z.id";
        List<PrecioZona> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            ps.setLong(2, idRecinto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PrecioZona p = new PrecioZona();
                    p.setIdZona(rs.getLong("id"));
                    p.setZona(rs.getString("nombre"));
                    p.setPrecio(rs.getBigDecimal("precio"));
                    p.setAsientosActivos(rs.getInt("activos"));
                    lista.add(p);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar los precios", ex);
        }
    }

    @Override
    public void guardarPrecios(long idEvento, Map<Long, BigDecimal> precios) {
        String sql = "INSERT INTO precio_zona (id_evento, id_zona, precio) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE precio = VALUES(precio)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            cn.setAutoCommit(false);
            try {
                for (Map.Entry<Long, BigDecimal> p : precios.entrySet()) {
                    ps.setLong(1, idEvento);
                    ps.setLong(2, p.getKey());
                    ps.setBigDecimal(3, p.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo guardar los precios", ex);
        }
    }

    @Override
    public List<Asiento> listarMapa(long idEvento) {
        // Una reserva vencida se muestra como Disponible (RN-02).
        String sql = "SELECT ea.id AS id_ea, a.id, a.id_zona, a.fila, a.numero, "
                + "CASE WHEN ea.estado = 'Reservado' AND ea.reservado_hasta < NOW() THEN 'Disponible' "
                + "ELSE ea.estado END AS estado "
                + "FROM evento_asiento ea JOIN asiento a ON a.id = ea.id_asiento "
                + "WHERE ea.id_evento = ? ORDER BY a.id_zona, LENGTH(a.fila), a.fila, a.numero";
        List<Asiento> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Asiento a = new Asiento();
                    a.setIdEventoAsiento(rs.getLong("id_ea"));
                    a.setId(rs.getLong("id"));
                    a.setIdZona(rs.getLong("id_zona"));
                    a.setFila(rs.getString("fila"));
                    a.setNumero(rs.getInt("numero"));
                    a.setEstado(rs.getString("estado"));
                    lista.add(a);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cargar el mapa de asientos", ex);
        }
    }

    @Override
    public List<OcupacionZona> ocupacionPorEvento(long idEvento) {
        String sql = "SELECT id_evento, zona, total_asientos, vendidos, reservados, disponibles, bloqueados "
                + "FROM v_ocupacion_evento_zona WHERE id_evento = ? ORDER BY id_zona";
        List<OcupacionZona> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OcupacionZona o = new OcupacionZona();
                    o.setIdEvento(rs.getLong("id_evento"));
                    o.setZona(rs.getString("zona"));
                    o.setTotal(rs.getInt("total_asientos"));
                    o.setVendidos(rs.getInt("vendidos"));
                    o.setReservados(rs.getInt("reservados"));
                    o.setDisponibles(rs.getInt("disponibles"));
                    o.setBloqueados(rs.getInt("bloqueados"));
                    lista.add(o);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar la ocupación", ex);
        }
    }

    @Override
    public boolean cambiarBloqueo(Connection cn, long idEvento, long idEventoAsiento, boolean bloquear) {
        String sql = "UPDATE evento_asiento SET estado = ? WHERE id = ? AND id_evento = ? AND estado = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, bloquear ? "Bloqueado" : "Disponible");
            ps.setLong(2, idEventoAsiento);
            ps.setLong(3, idEvento);
            ps.setString(4, bloquear ? "Disponible" : "Bloqueado");
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cambiar el asiento", ex);
        }
    }

    @Override
    public int contarAsientosActivos(Connection cn, long idRecinto) {
        String sql = "SELECT COUNT(*) FROM asiento a JOIN zona z ON z.id = a.id_zona "
                + "WHERE z.id_recinto = ? AND a.activo = 1";
        return contar(cn, sql, idRecinto);
    }

    @Override
    public int contarZonasSinPrecio(Connection cn, long idEvento, long idRecinto) {
        String sql = "SELECT COUNT(*) FROM zona z WHERE z.id_recinto = ? "
                + "AND EXISTS (SELECT 1 FROM asiento a WHERE a.id_zona = z.id AND a.activo = 1) "
                + "AND NOT EXISTS (SELECT 1 FROM precio_zona pz WHERE pz.id_zona = z.id AND pz.id_evento = ?)";
        return contar(cn, sql, idRecinto, idEvento);
    }

    @Override
    public int generarDisponibilidad(Connection cn, long idEvento, long idRecinto) {
        String sql = "INSERT INTO evento_asiento (id_evento, id_asiento) "
                + "SELECT ?, a.id FROM asiento a JOIN zona z ON z.id = a.id_zona "
                + "WHERE z.id_recinto = ? AND a.activo = 1 "
                + "AND NOT EXISTS (SELECT 1 FROM evento_asiento ea WHERE ea.id_evento = ? AND ea.id_asiento = a.id)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            ps.setLong(2, idRecinto);
            ps.setLong(3, idEvento);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar la disponibilidad de asientos", ex);
        }
    }

    @Override
    public int contarLibres(Connection cn, long idEvento) {
        String sql = "SELECT COUNT(*) FROM evento_asiento WHERE id_evento = ? AND (estado = 'Disponible' "
                + "OR (estado = 'Reservado' AND reservado_hasta < NOW()))";
        return contar(cn, sql, idEvento);
    }

    private int contar(Connection cn, String sql, long... parametros) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setLong(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo realizar el conteo", ex);
        }
    }
}
