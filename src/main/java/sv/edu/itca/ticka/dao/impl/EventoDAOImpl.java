package sv.edu.itca.ticka.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.modelo.Afiche;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroEvento;
import sv.edu.itca.ticka.modelo.PrecioZona;
import sv.edu.itca.ticka.modelo.TipoEvento;
import sv.edu.itca.ticka.persistencia.ConexionBD;

/** Implementación JDBC de EventoDAO. */
public class EventoDAOImpl implements EventoDAO {

    /** SELECT con JOIN para traer el nombre de la categoría y del recinto. */
    private static final String SQL_SELECT
            = "SELECT e.id, e.nombre, e.tipo, e.descripcion, e.participantes, (e.afiche IS NOT NULL) AS con_afiche, "
            + "e.id_categoria, e.id_recinto, e.apertura_puertas, e.fecha_inicio, e.fecha_fin, "
            + "e.estado, e.creado_por, c.nombre AS categoria, r.nombre AS recinto, "
            + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id) AS total_asientos, "
            + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id "
            + " AND ea.estado = 'Vendido') AS vendidos "
            + "FROM evento e "
            + "LEFT JOIN categoria c ON c.id = e.id_categoria "
            + "JOIN recinto r ON r.id = e.id_recinto ";

    private Evento mapear(ResultSet rs) throws SQLException {
        Evento e = new Evento();
        e.setId(rs.getLong("id"));
        e.setNombre(rs.getString("nombre"));
        e.setTipo(TipoEvento.desdeBd(rs.getString("tipo")));
        e.setDescripcion(rs.getString("descripcion"));
        e.setParticipantes(rs.getString("participantes"));
        e.setConAfiche(rs.getBoolean("con_afiche"));
        long categoria = rs.getLong("id_categoria");
        e.setIdCategoria(rs.wasNull() ? null : categoria);
        e.setIdRecinto(rs.getLong("id_recinto"));
        e.setAperturaPuertas(rs.getObject("apertura_puertas", LocalDateTime.class));
        e.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
        e.setFechaFin(rs.getObject("fecha_fin", LocalDateTime.class));
        e.setEstado(EstadoEvento.desdeBd(rs.getString("estado")));
        long creador = rs.getLong("creado_por");
        e.setCreadoPor(rs.wasNull() ? null : creador);
        e.setCategoria(rs.getString("categoria"));
        e.setRecinto(rs.getString("recinto"));
        e.setTotalAsientos(rs.getInt("total_asientos"));
        e.setVendidos(rs.getInt("vendidos"));
        return e;
    }

    /** Arma el WHERE dinámico del filtro y acumula sus parámetros en orden. */
    private String condiciones(FiltroEvento f, List<Object> parametros) {
        StringBuilder sb = new StringBuilder("WHERE e.activo = 1 ");
        if (f.getTexto() != null) {
            sb.append("AND (e.nombre LIKE ? OR e.participantes LIKE ?) ");
            parametros.add("%" + f.getTexto() + "%");
            parametros.add("%" + f.getTexto() + "%");
        }
        if (f.getTipo() != null) {
            sb.append("AND e.tipo = ? ");
            parametros.add(f.getTipo().getBd());
        }
        if (f.getEstado() != null) {
            sb.append("AND e.estado = ? ");
            parametros.add(f.getEstado().getBd());
        }
        if (f.getIdRecinto() != null) {
            sb.append("AND e.id_recinto = ? ");
            parametros.add(f.getIdRecinto());
        }
        if (f.getDesde() != null) {
            sb.append("AND e.fecha_inicio >= ? ");
            parametros.add(f.getDesde().atStartOfDay());
        }
        if (f.getHasta() != null) {
            sb.append("AND e.fecha_inicio < ? ");
            parametros.add(f.getHasta().plusDays(1).atStartOfDay());
        }
        return sb.toString();
    }

    private static void asignar(PreparedStatement ps, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            ps.setObject(i + 1, parametros.get(i));
        }
    }

    @Override
    public List<Evento> listar(FiltroEvento f) {
        List<Object> parametros = new ArrayList<>();
        String sql = SQL_SELECT + condiciones(f, parametros) + "ORDER BY e.fecha_inicio DESC LIMIT ? OFFSET ?";
        parametros.add(FiltroEvento.TAMANO_PAGINA);
        parametros.add(f.getDesplazamiento());
        List<Evento> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los eventos", ex);
        }
    }

    @Override
    public int contar(FiltroEvento f) {
        List<Object> parametros = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM evento e " + condiciones(f, parametros);
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo contar los eventos", ex);
        }
    }

    @Override
    public Optional<Evento> buscarPorId(long id) {
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(SQL_SELECT + "WHERE e.id = ? AND e.activo = 1")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el evento", ex);
        }
    }

    @Override
    public long insertar(Evento e) {
        String sql = "INSERT INTO evento (nombre, tipo, descripcion, participantes, id_categoria, "
                + "id_recinto, apertura_puertas, fecha_inicio, fecha_fin, estado, creado_por) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, e);
            ps.setString(10, EstadoEvento.BORRADOR.getBd());
            ps.setObject(11, e.getCreadoPor());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                long id = rs.getLong(1);
                e.setId(id);
                return id;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo registrar el evento", ex);
        }
    }

    @Override
    public boolean actualizar(Evento e) {
        String sql = "UPDATE evento SET nombre = ?, tipo = ?, descripcion = ?, participantes = ?, "
                + "id_categoria = ?, id_recinto = ?, apertura_puertas = ?, fecha_inicio = ?, fecha_fin = ? "
                + "WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarDatos(ps, e);
            ps.setLong(10, e.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el evento", ex);
        }
    }

    /** Parámetros 1 a 9, comunes a INSERT y UPDATE (el afiche se guarda aparte). */
    private static void asignarDatos(PreparedStatement ps, Evento e) throws SQLException {
        ps.setString(1, e.getNombre());
        ps.setString(2, e.getTipo().getBd());
        ps.setString(3, e.getDescripcion());
        ps.setString(4, e.getParticipantes());
        ps.setObject(5, e.getIdCategoria());
        ps.setLong(6, e.getIdRecinto());
        ps.setObject(7, e.getAperturaPuertas());
        ps.setObject(8, e.getFechaInicio());
        ps.setObject(9, e.getFechaFin());
    }

    @Override
    public void guardarAfiche(long idEvento, byte[] contenido, String tipo) {
        String sql = "UPDATE evento SET afiche = ?, afiche_tipo = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBytes(1, contenido);
            ps.setString(2, tipo);
            ps.setLong(3, idEvento);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo guardar el afiche", ex);
        }
    }

    @Override
    public void quitarAfiche(long idEvento) {
        guardarAfiche(idEvento, null, null);
    }

    @Override
    public Optional<Afiche> obtenerAfiche(long idEvento) {
        String sql = "SELECT afiche, afiche_tipo FROM evento WHERE id = ? AND activo = 1 AND afiche IS NOT NULL";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new Afiche(rs.getBytes("afiche"), rs.getString("afiche_tipo")))
                        : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo leer el afiche", ex);
        }
    }

    @Override
    public boolean existeChoque(long idRecinto, LocalDateTime inicio, LocalDateTime fin, Long excluirId) {
        // Dos rangos se traslapan si cada uno empieza antes de que termine el otro.
        String sql = "SELECT 1 FROM evento WHERE id_recinto = ? AND activo = 1 AND estado <> 'Cancelado' "
                + "AND (? IS NULL OR id <> ?) AND fecha_inicio < ? AND fecha_fin > ? LIMIT 1";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idRecinto);
            ps.setObject(2, excluirId);
            ps.setObject(3, excluirId);
            ps.setObject(4, fin);
            ps.setObject(5, inicio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar el horario del recinto", ex);
        }
    }

    @Override
    public boolean tieneVentas(long idEvento) {
        String sql = "SELECT 1 FROM entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "WHERE ea.id_evento = ? AND en.estado <> 'Anulada' LIMIT 1";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo verificar las ventas del evento", ex);
        }
    }

    @Override
    public List<Evento> listarCartelera(TipoEvento tipo) {
        String condicion = "e.activo = 1 AND e.estado IN ('Publicado', 'Agotado', 'En curso') AND e.fecha_fin >= NOW() ";
        String sqlEventos = "SELECT e.id, e.nombre, e.tipo, e.descripcion, e.participantes, "
                + "(e.afiche IS NOT NULL) AS con_afiche, e.id_recinto, "
                + "e.apertura_puertas, e.fecha_inicio, e.fecha_fin, e.estado, c.nombre AS categoria, r.nombre AS recinto, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id) AS total, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id AND ea.estado = 'Vendido') AS vendidos, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id AND (ea.estado = 'Disponible' "
                + " OR (ea.estado = 'Reservado' AND ea.reservado_hasta < NOW()))) AS disponibles, "
                + "(SELECT MIN(pz.precio) FROM precio_zona pz WHERE pz.id_evento = e.id) AS precio_min "
                + "FROM evento e LEFT JOIN categoria c ON c.id = e.id_categoria JOIN recinto r ON r.id = e.id_recinto "
                + "WHERE " + condicion + "AND (? IS NULL OR e.tipo = ?) ORDER BY e.fecha_inicio";
        // Precios por zona de los mismos eventos, del más caro al más barato.
        String sqlPrecios = "SELECT pz.id_evento, z.nombre, pz.precio FROM precio_zona pz "
                + "JOIN zona z ON z.id = pz.id_zona JOIN evento e ON e.id = pz.id_evento "
                + "WHERE " + condicion + "ORDER BY pz.id_evento, pz.precio DESC";

        Map<Long, Evento> eventos = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.obtenerConexion()) {
            try (PreparedStatement ps = cn.prepareStatement(sqlEventos)) {
                String valorTipo = tipo == null ? null : tipo.getBd();
                ps.setString(1, valorTipo);
                ps.setString(2, valorTipo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Evento e = new Evento();
                        e.setId(rs.getLong("id"));
                        e.setNombre(rs.getString("nombre"));
                        e.setTipo(TipoEvento.desdeBd(rs.getString("tipo")));
                        e.setDescripcion(rs.getString("descripcion"));
                        e.setParticipantes(rs.getString("participantes"));
                        e.setConAfiche(rs.getBoolean("con_afiche"));
                        e.setIdRecinto(rs.getLong("id_recinto"));
                        e.setAperturaPuertas(rs.getObject("apertura_puertas", LocalDateTime.class));
                        e.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
                        e.setFechaFin(rs.getObject("fecha_fin", LocalDateTime.class));
                        e.setEstado(EstadoEvento.desdeBd(rs.getString("estado")));
                        e.setCategoria(rs.getString("categoria"));
                        e.setRecinto(rs.getString("recinto"));
                        e.setTotalAsientos(rs.getInt("total"));
                        e.setVendidos(rs.getInt("vendidos"));
                        e.setDisponibles(rs.getInt("disponibles"));
                        e.setPrecioMinimo(rs.getBigDecimal("precio_min"));
                        eventos.put(e.getId(), e);
                    }
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlPrecios);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evento e = eventos.get(rs.getLong("id_evento"));
                    if (e != null) {
                        PrecioZona p = new PrecioZona();
                        p.setZona(rs.getString("nombre"));
                        p.setPrecio(rs.getBigDecimal("precio"));
                        e.getPrecios().add(p);
                    }
                }
            }
            return new ArrayList<>(eventos.values());
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cargar la cartelera", ex);
        }
    }

    @Override
    public Optional<Evento> buscarParaActualizar(Connection cn, long id) {
        String sql = SQL_SELECT + "WHERE e.id = ? AND e.activo = 1 FOR UPDATE";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar el evento", ex);
        }
    }

    @Override
    public void cambiarEstado(Connection cn, long id, EstadoEvento estado) {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE evento SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.getBd());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo cambiar el estado del evento", ex);
        }
    }

    @Override
    public int anularVentas(Connection cn, long idEvento, String motivo) {
        String sqlVentas = "UPDATE venta SET estado = 'Anulada', motivo_anulacion = ? "
                + "WHERE estado = 'Pagada' AND id IN (SELECT en.id_venta FROM entrada en "
                + "JOIN evento_asiento ea ON ea.id = en.id_evento_asiento WHERE ea.id_evento = ?)";
        String sqlBoletos = "UPDATE entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "SET en.estado = 'Anulada' WHERE ea.id_evento = ? AND en.estado <> 'Anulada'";
        String sqlAsientos = "UPDATE evento_asiento SET estado = 'Disponible', reservado_hasta = NULL, "
                + "id_cliente_reserva = NULL WHERE id_evento = ? AND estado IN ('Vendido', 'Reservado')";
        try (PreparedStatement psVentas = cn.prepareStatement(sqlVentas);
                PreparedStatement psBoletos = cn.prepareStatement(sqlBoletos);
                PreparedStatement psAsientos = cn.prepareStatement(sqlAsientos)) {
            psVentas.setString(1, motivo);
            psVentas.setLong(2, idEvento);
            psVentas.executeUpdate();
            psBoletos.setLong(1, idEvento);
            int boletos = psBoletos.executeUpdate();
            psAsientos.setLong(1, idEvento);
            psAsientos.executeUpdate();
            return boletos;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo anular las ventas del evento", ex);
        }
    }
}
