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
import sv.edu.itca.ticka.dao.AccesoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.modelo.BoletoPuerta;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.OcupacionPuerta;
import sv.edu.itca.ticka.modelo.RegistroAcceso;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de AccesoDAO. */
public class AccesoDAOImpl implements AccesoDAO {

    /** Boleto con su evento, asiento y cliente. */
    private static final String SQL_BOLETO = "SELECT en.id, en.codigo, en.estado, ev.id AS id_evento, "
            + "ev.nombre AS evento, z.nombre AS zona, a.fila, a.numero, "
            + "CONCAT(c.nombres, ' ', c.apellidos) AS cliente, c.documento "
            + "FROM entrada en "
            + "JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
            + "JOIN evento ev ON ev.id = ea.id_evento "
            + "JOIN asiento a ON a.id = ea.id_asiento "
            + "JOIN zona z ON z.id = a.id_zona "
            + "JOIN venta v ON v.id = en.id_venta "
            + "JOIN cliente c ON c.id = v.id_cliente ";

    private static BoletoPuerta mapearBoleto(ResultSet rs) throws SQLException {
        BoletoPuerta b = new BoletoPuerta();
        b.setIdEntrada(rs.getLong("id"));
        b.setCodigo(rs.getString("codigo"));
        b.setEstado(rs.getString("estado"));
        b.setIdEvento(rs.getLong("id_evento"));
        b.setEvento(rs.getString("evento"));
        b.setZona(rs.getString("zona"));
        b.setFila(rs.getString("fila"));
        b.setNumero(rs.getInt("numero"));
        b.setCliente(rs.getString("cliente"));
        b.setDocumento(rs.getString("documento"));
        return b;
    }

    @Override
    public List<Evento> eventosParaPuerta() {
        String sql = "SELECT e.id, e.nombre, e.apertura_puertas, e.fecha_inicio, e.fecha_fin, e.estado, r.nombre AS recinto "
                + "FROM evento e JOIN recinto r ON r.id = e.id_recinto "
                + "WHERE e.activo = 1 AND e.estado IN ('Publicado', 'Agotado', 'En curso', 'Finalizado') "
                + "AND e.fecha_fin >= NOW() - INTERVAL 1 DAY ORDER BY e.fecha_inicio";
        List<Evento> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evento e = new Evento();
                e.setId(rs.getLong("id"));
                e.setNombre(rs.getString("nombre"));
                e.setAperturaPuertas(rs.getObject("apertura_puertas", LocalDateTime.class));
                e.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
                e.setFechaFin(rs.getObject("fecha_fin", LocalDateTime.class));
                e.setEstado(EstadoEvento.desdeBd(rs.getString("estado")));
                e.setRecinto(rs.getString("recinto"));
                lista.add(e);
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los eventos para la puerta", ex);
        }
    }

    @Override
    public Optional<BoletoPuerta> buscarPorCodigo(Connection cn, String codigo) {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BOLETO + "WHERE en.codigo = ? FOR UPDATE")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearBoleto(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo buscar el boleto", ex);
        }
    }

    @Override
    public List<BoletoPuerta> porDocumento(long idEvento, String documento) {
        String sql = SQL_BOLETO + "WHERE ev.id = ? AND c.documento = ? ORDER BY z.id, a.fila, a.numero";
        List<BoletoPuerta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            ps.setString(2, documento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearBoleto(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo buscar los boletos del cliente", ex);
        }
    }

    @Override
    public long registrar(Connection cn, long idEvento, Long idEntrada, String codigo, String puerta, long idUsuario,
            boolean valido, String motivo) {
        String sql = "INSERT INTO acceso (id_evento, id_entrada, codigo_leido, puerta, id_usuario, resultado, motivo_rechazo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, idEvento);
            ps.setObject(2, idEntrada);
            ps.setString(3, codigo.length() > 40 ? codigo.substring(0, 40) : codigo);
            ps.setString(4, puerta);
            ps.setLong(5, idUsuario);
            ps.setString(6, valido ? "Valido" : "Rechazado");
            ps.setString(7, motivo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar el acceso", ex);
        }
    }

    @Override
    public void cambiarEstadoEntrada(Connection cn, long idEntrada, String estado) {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE entrada SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setLong(2, idEntrada);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el boleto", ex);
        }
    }

    @Override
    public List<RegistroAcceso> recientes(long idEvento, int limite) {
        String sql = "SELECT ac.id, ac.id_entrada, ac.id_evento, ac.fecha_hora, ac.codigo_leido, ac.puerta, ac.resultado, "
                + "ac.motivo_rechazo, ac.anulado, ac.motivo_anulacion, u.nombre AS validador, "
                + "CONCAT(z.nombre, ' ', a.fila, '-', a.numero) AS asiento "
                + "FROM acceso ac JOIN usuario u ON u.id = ac.id_usuario "
                + "LEFT JOIN entrada en ON en.id = ac.id_entrada "
                + "LEFT JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "LEFT JOIN asiento a ON a.id = ea.id_asiento "
                + "LEFT JOIN zona z ON z.id = a.id_zona "
                + "WHERE ac.id_evento = ? ORDER BY ac.fecha_hora DESC, ac.id DESC LIMIT ?";
        List<RegistroAcceso> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAcceso(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar los accesos", ex);
        }
    }

    private static RegistroAcceso mapearAcceso(ResultSet rs) throws SQLException {
        RegistroAcceso r = new RegistroAcceso();
        r.setId(rs.getLong("id"));
        long entrada = rs.getLong("id_entrada");
        r.setIdEntrada(rs.wasNull() ? null : entrada);
        r.setIdEvento(rs.getLong("id_evento"));
        r.setFechaHora(rs.getObject("fecha_hora", LocalDateTime.class));
        r.setCodigo(rs.getString("codigo_leido"));
        r.setPuerta(rs.getString("puerta"));
        r.setResultado(rs.getString("resultado"));
        r.setMotivoRechazo(rs.getString("motivo_rechazo"));
        r.setAnulado(rs.getBoolean("anulado"));
        r.setMotivoAnulacion(rs.getString("motivo_anulacion"));
        r.setValidador(rs.getString("validador"));
        r.setAsiento(rs.getString("asiento"));
        return r;
    }

    @Override
    public Optional<RegistroAcceso> buscarParaAnular(Connection cn, long idAcceso) {
        String sql = "SELECT ac.id, ac.id_entrada, ac.id_evento, ac.fecha_hora, ac.codigo_leido, ac.puerta, ac.resultado, "
                + "ac.motivo_rechazo, ac.anulado, ac.motivo_anulacion, '' AS validador, NULL AS asiento "
                + "FROM acceso ac WHERE ac.id = ? FOR UPDATE";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idAcceso);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearAcceso(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el acceso", ex);
        }
    }

    @Override
    public void anular(Connection cn, long idAcceso, String motivo) {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE acceso SET anulado = 1, motivo_anulacion = ? WHERE id = ?")) {
            ps.setString(1, motivo);
            ps.setLong(2, idAcceso);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo anular el ingreso", ex);
        }
    }

    @Override
    public int contarIngresosValidos(Connection cn, long idEntrada) {
        String sql = "SELECT COUNT(*) FROM acceso WHERE id_entrada = ? AND resultado = 'Valido' AND anulado = 0";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEntrada);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo contar los ingresos", ex);
        }
    }

    @Override
    public List<OcupacionPuerta> ocupacion(long idEvento) {
        String sql = "SELECT z.nombre, COUNT(en.id) AS vendidos, SUM(en.estado = 'Usada') AS ingresados "
                + "FROM entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "JOIN asiento a ON a.id = ea.id_asiento JOIN zona z ON z.id = a.id_zona "
                + "WHERE ea.id_evento = ? AND en.estado <> 'Anulada' GROUP BY z.id, z.nombre ORDER BY z.id";
        List<OcupacionPuerta> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OcupacionPuerta(rs.getString(1), rs.getInt(2), rs.getInt(3)));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo calcular la ocupación", ex);
        }
    }
}
