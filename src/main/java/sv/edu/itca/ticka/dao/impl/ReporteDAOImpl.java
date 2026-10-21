package sv.edu.itca.ticka.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.ReporteDAO;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroReporte;
import sv.edu.itca.ticka.modelo.Indicadores;
import sv.edu.itca.ticka.modelo.Reporte;
import sv.edu.itca.ticka.modelo.TipoEvento;
import sv.edu.itca.ticka.persistencia.ConexionBD;
import sv.edu.itca.ticka.util.Formatos;

/** Implementación JDBC de ReporteDAO (solo consultas SELECT). */
public class ReporteDAOImpl implements ReporteDAO {

    private static final String ESTADOS_ACTIVOS = "('Publicado', 'Agotado', 'En curso')";

    /**
     * Filtro común sobre el evento "ev" (RF-48): fechas, evento, tipo y
     * categoría. Agrega los parámetros a la lista en el mismo orden del SQL.
     */
    private static String filtroEvento(FiltroReporte f, List<Object> parametros) {
        StringBuilder sb = new StringBuilder("ev.activo = 1 ");
        if (f.getDesde() != null) {
            sb.append("AND ev.fecha_inicio >= ? ");
            parametros.add(f.getDesde().atStartOfDay());
        }
        if (f.getHasta() != null) {
            sb.append("AND ev.fecha_inicio < ? ");
            parametros.add(f.getHasta().plusDays(1).atStartOfDay());
        }
        if (f.getIdEvento() != null) {
            sb.append("AND ev.id = ? ");
            parametros.add(f.getIdEvento());
        }
        if (f.getTipoEvento() != null) {
            sb.append("AND ev.tipo = ? ");
            parametros.add(f.getTipoEvento().getBd());
        }
        if (f.getIdCategoria() != null) {
            sb.append("AND ev.id_categoria = ? ");
            parametros.add(f.getIdCategoria());
        }
        return sb.toString();
    }

    /** Condición de zona sobre la columna indicada, si el filtro la trae. */
    private static String filtroZona(String columna, FiltroReporte f, List<Object> parametros) {
        if (f.getIdZona() == null) {
            return "";
        }
        parametros.add(f.getIdZona());
        return "AND " + columna + " = ? ";
    }

    private static void asignar(PreparedStatement ps, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            ps.setObject(i + 1, parametros.get(i));
        }
    }

    @Override
    public Indicadores indicadores() {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM evento WHERE activo = 1 AND estado IN " + ESTADOS_ACTIVOS + ") AS eventos_activos, "
                + "(SELECT COUNT(*) FROM entrada WHERE estado <> 'Anulada') AS boletos, "
                + "(SELECT COALESCE(SUM(precio), 0) FROM entrada WHERE estado <> 'Anulada') AS ingresos, "
                + "(SELECT COUNT(*) FROM evento_asiento ea JOIN evento e ON e.id = ea.id_evento "
                + " WHERE e.activo = 1 AND e.estado IN " + ESTADOS_ACTIVOS + " AND ea.estado = 'Vendido') AS vendidos_activos, "
                + "(SELECT COUNT(*) FROM evento_asiento ea JOIN evento e ON e.id = ea.id_evento "
                + " WHERE e.activo = 1 AND e.estado IN " + ESTADOS_ACTIVOS + ") AS total_activos, "
                + "(SELECT COUNT(DISTINCT ac.id_entrada) FROM acceso ac JOIN evento e ON e.id = ac.id_evento "
                + " WHERE e.estado = 'Finalizado' AND ac.resultado = 'Valido' AND ac.anulado = 0) AS ingresados_fin, "
                + "(SELECT COUNT(*) FROM entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + " JOIN evento e ON e.id = ea.id_evento WHERE e.estado = 'Finalizado' AND en.estado <> 'Anulada') AS vendidos_fin, "
                + "(SELECT COUNT(*) FROM incidencia WHERE estado <> 'Cerrada') AS incidencias, "
                + "(SELECT COUNT(*) FROM cliente c JOIN usuario u ON u.id = c.id_usuario WHERE u.activo = 1) AS clientes";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            Indicadores i = new Indicadores();
            if (rs.next()) {
                i.setEventosActivos(rs.getInt("eventos_activos"));
                i.setBoletosVendidos(rs.getInt("boletos"));
                i.setIngresos(rs.getBigDecimal("ingresos"));
                i.setAsientosVendidosActivos(rs.getInt("vendidos_activos"));
                i.setAsientosTotalesActivos(rs.getInt("total_activos"));
                i.setIngresadosFinalizados(rs.getInt("ingresados_fin"));
                i.setVendidosFinalizados(rs.getInt("vendidos_fin"));
                i.setIncidenciasAbiertas(rs.getInt("incidencias"));
                i.setClientesActivos(rs.getInt("clientes"));
            }
            return i;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo calcular los indicadores", ex);
        }
    }

    @Override
    public List<Evento> proximosEventos(int limite) {
        String sql = "SELECT e.id, e.nombre, e.tipo, e.fecha_inicio, e.fecha_fin, e.estado, r.nombre AS recinto, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id) AS total, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = e.id AND ea.estado = 'Vendido') AS vendidos "
                + "FROM evento e JOIN recinto r ON r.id = e.id_recinto "
                + "WHERE e.activo = 1 AND e.estado IN " + ESTADOS_ACTIVOS + " ORDER BY e.fecha_inicio LIMIT ?";
        List<Evento> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evento e = new Evento();
                    e.setId(rs.getLong("id"));
                    e.setNombre(rs.getString("nombre"));
                    e.setTipo(TipoEvento.desdeBd(rs.getString("tipo")));
                    e.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
                    e.setFechaFin(rs.getObject("fecha_fin", LocalDateTime.class));
                    e.setEstado(EstadoEvento.desdeBd(rs.getString("estado")));
                    e.setRecinto(rs.getString("recinto"));
                    e.setTotalAsientos(rs.getInt("total"));
                    e.setVendidos(rs.getInt("vendidos"));
                    lista.add(e);
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo consultar los próximos eventos", ex);
        }
    }

    @Override
    public List<Evento> eventosConMapa() {
        String sql = "SELECT id, nombre, fecha_inicio FROM evento WHERE activo = 1 AND estado <> 'Borrador' "
                + "ORDER BY fecha_inicio DESC";
        List<Evento> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evento e = new Evento();
                e.setId(rs.getLong("id"));
                e.setNombre(rs.getString("nombre"));
                e.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
                lista.add(e);
            }
            return lista;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los eventos", ex);
        }
    }

    @Override
    public Reporte ventas(FiltroReporte f) {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT ev.nombre, ev.fecha_inicio, z.nombre AS zona, v.canal, "
                + "COALESCE(u.nombre, 'Portal web') AS vendedor, "
                + "COUNT(*) AS boletos, SUM(en.precio) AS total "
                + "FROM entrada en "
                + "JOIN venta v ON v.id = en.id_venta "
                + "LEFT JOIN usuario u ON u.id = v.id_usuario_vendedor "
                + "JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "JOIN asiento a ON a.id = ea.id_asiento "
                + "JOIN zona z ON z.id = a.id_zona "
                + "JOIN evento ev ON ev.id = ea.id_evento "
                + "WHERE en.estado <> 'Anulada' AND " + filtroEvento(f, p) + filtroZona("z.id", f, p)
                + "GROUP BY ev.id, ev.nombre, ev.fecha_inicio, z.id, z.nombre, v.canal, vendedor "
                + "ORDER BY ev.fecha_inicio, ev.nombre, z.id, v.canal, vendedor";
        Reporte r = new Reporte("Ventas por evento, zona, canal y vendedor",
                "Evento", "Fecha", "Zona", "Canal", "Vendedor", "Boletos", "Ingresos").numericas(5, 6);
        int boletos = 0;
        BigDecimal total = BigDecimal.ZERO;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int b = rs.getInt("boletos");
                    BigDecimal t = rs.getBigDecimal("total");
                    boletos += b;
                    total = total.add(t);
                    r.agregarFila(rs.getString("nombre"),
                            Formatos.fecha(rs.getObject("fecha_inicio", LocalDateTime.class)),
                            rs.getString("zona"), rs.getString("canal"), rs.getString("vendedor"),
                            b, Formatos.dinero(t));
                }
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar el reporte de ventas", ex);
        }
        r.setTotales("Total", "", "", "", "", boletos, Formatos.dinero(total));
        return r;
    }

    @Override
    public Reporte asistencia(FiltroReporte f) {
        List<Object> p = new ArrayList<>();
        // Con filtro de zona, solo se cuentan los boletos y accesos de asientos de esa zona.
        boolean porZona = f.getIdZona() != null;
        String asientoZona = porZona
                ? "JOIN asiento a ON a.id = ea.id_asiento AND a.id_zona = ? " : "";
        String accesoZona = porZona
                ? "JOIN entrada en ON en.id = ac.id_entrada JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "JOIN asiento a ON a.id = ea.id_asiento AND a.id_zona = ? " : "";
        if (porZona) {
            // Un parámetro por cada subconsulta (vendidos, ingresados y rechazados).
            p.add(f.getIdZona());
            p.add(f.getIdZona());
            p.add(f.getIdZona());
        }
        String sql = "SELECT * FROM (SELECT ev.nombre, ev.fecha_inicio, ev.estado, "
                + "(SELECT COUNT(*) FROM entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + asientoZona + "WHERE ea.id_evento = ev.id AND en.estado <> 'Anulada') AS vendidos, "
                + "(SELECT COUNT(DISTINCT ac.id_entrada) FROM acceso ac " + accesoZona + "WHERE ac.id_evento = ev.id "
                + "AND ac.resultado = 'Valido' AND ac.anulado = 0) AS ingresados, "
                + "(SELECT COUNT(*) FROM acceso ac " + accesoZona
                + "WHERE ac.id_evento = ev.id AND ac.resultado = 'Rechazado') AS rechazados "
                + "FROM evento ev WHERE " + filtroEvento(f, p) + ") t "
                + "WHERE vendidos > 0 OR ingresados > 0 ORDER BY fecha_inicio";
        Reporte r = new Reporte("Asistencia: boletos vendidos frente a ingresos en puerta",
                "Evento", "Fecha", "Estado", "Vendidos", "Ingresaron", "Ausentes", "% asistencia", "Rechazos en puerta")
                .numericas(3, 4, 5, 6, 7);
        int totalVendidos = 0;
        int totalIngresados = 0;
        int totalRechazos = 0;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int vendidos = rs.getInt("vendidos");
                    int ingresados = rs.getInt("ingresados");
                    int rechazos = rs.getInt("rechazados");
                    totalVendidos += vendidos;
                    totalIngresados += ingresados;
                    totalRechazos += rechazos;
                    r.agregarFila(rs.getString("nombre"),
                            Formatos.fecha(rs.getObject("fecha_inicio", LocalDateTime.class)),
                            rs.getString("estado"), vendidos, ingresados, Math.max(0, vendidos - ingresados),
                            Formatos.porcentaje(ingresados, vendidos) + "%", rechazos);
                }
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar el reporte de asistencia", ex);
        }
        r.setTotales("Total", "", "", totalVendidos, totalIngresados, Math.max(0, totalVendidos - totalIngresados),
                Formatos.porcentaje(totalIngresados, totalVendidos) + "%", totalRechazos);
        return r;
    }

    @Override
    public Reporte ocupacion(FiltroReporte f) {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT ev.nombre, ev.fecha_inicio, ev.estado, o.zona, o.total_asientos, o.vendidos, "
                + "o.reservados, o.disponibles, o.bloqueados "
                + "FROM v_ocupacion_evento_zona o JOIN evento ev ON ev.id = o.id_evento "
                + "WHERE ev.estado <> 'Cancelado' AND " + filtroEvento(f, p) + filtroZona("o.id_zona", f, p)
                + "ORDER BY ev.fecha_inicio, ev.nombre, o.id_zona";
        Reporte r = new Reporte("Ocupación de asientos por evento y zona",
                "Evento", "Fecha", "Estado", "Zona", "Total", "Vendidos", "Reservados", "Disponibles",
                "Bloqueados", "% vendido").numericas(4, 5, 6, 7, 8, 9);
        int total = 0;
        int vendidos = 0;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int t = rs.getInt("total_asientos");
                    int v = rs.getInt("vendidos");
                    total += t;
                    vendidos += v;
                    r.agregarFila(rs.getString("nombre"),
                            Formatos.fecha(rs.getObject("fecha_inicio", LocalDateTime.class)),
                            rs.getString("estado"), rs.getString("zona"), t, v, rs.getInt("reservados"),
                            rs.getInt("disponibles"), rs.getInt("bloqueados"), Formatos.porcentaje(v, t) + "%");
                }
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar el reporte de ocupación", ex);
        }
        r.setTotales("Total", "", "", "", total, vendidos, "", "", "", Formatos.porcentaje(vendidos, total) + "%");
        return r;
    }

    @Override
    public Reporte incidencias(FiltroReporte f) {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT i.fecha_hora, ev.nombre, i.tipo, i.gravedad, i.estado, i.puerta, i.descripcion "
                + "FROM incidencia i JOIN evento ev ON ev.id = i.id_evento "
                + "WHERE " + filtroEvento(f, p)
                + "ORDER BY i.fecha_hora DESC";
        Reporte r = new Reporte("Incidencias registradas en los eventos",
                "Fecha y hora", "Evento", "Tipo", "Gravedad", "Estado", "Puerta", "Descripción");
        int cantidad = 0;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cantidad++;
                    r.agregarFila(Formatos.fechaHora(rs.getObject("fecha_hora", LocalDateTime.class)),
                            rs.getString("nombre"), rs.getString("tipo"), rs.getString("gravedad"),
                            rs.getString("estado"), rs.getString("puerta"), rs.getString("descripcion"));
                }
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar el reporte de incidencias", ex);
        }
        r.setTotales("Total: " + cantidad, "", "", "", "", "", "");
        return r;
    }

    @Override
    public Map<Long, String> zonasParaFiltro() {
        String sql = "SELECT z.id, r.nombre AS recinto, z.nombre FROM zona z "
                + "JOIN recinto r ON r.id = z.id_recinto ORDER BY r.nombre, z.id";
        Map<Long, String> zonas = new LinkedHashMap<>();
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                zonas.put(rs.getLong("id"), rs.getString("recinto") + " · " + rs.getString("nombre"));
            }
            return zonas;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar las zonas", ex);
        }
    }
}
