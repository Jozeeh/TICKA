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
    public Reporte general(FiltroReporte f) {
        return f.getIdEvento() == null ? resumenPorEvento(f) : detalleEvento(f.getIdEvento());
    }

    /** Sin evento elegido: una fila por evento con sus cifras principales. */
    private Reporte resumenPorEvento(FiltroReporte f) {
        List<Object> p = new ArrayList<>();
        String sql = "SELECT ev.nombre, ev.fecha_inicio, ev.estado, r.nombre AS recinto, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = ev.id) AS asientos, "
                + "(SELECT COUNT(*) FROM evento_asiento ea WHERE ea.id_evento = ev.id AND ea.estado = 'Vendido') AS vendidos, "
                + "(SELECT COALESCE(SUM(en.precio), 0) FROM entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + " WHERE ea.id_evento = ev.id AND en.estado <> 'Anulada') AS ingresos, "
                + "(SELECT COUNT(DISTINCT ac.id_entrada) FROM acceso ac WHERE ac.id_evento = ev.id "
                + " AND ac.resultado = 'Valido' AND ac.anulado = 0) AS ingresados, "
                + "(SELECT COUNT(*) FROM incidencia i WHERE i.id_evento = ev.id) AS incidencias "
                + "FROM evento ev JOIN recinto r ON r.id = ev.id_recinto "
                + "WHERE ev.estado <> 'Borrador' AND " + filtroEvento(f, p)
                + "ORDER BY ev.fecha_inicio";
        Reporte r = new Reporte("Resumen general por evento",
                "Evento", "Fecha", "Estado", "Recinto", "Asientos", "Vendidos", "Sin vender", "% ocupación",
                "Ingresos", "Ingresaron", "% asistencia", "Incidencias").numericas(4, 5, 6, 7, 8, 9, 10, 11);
        int asientos = 0;
        int vendidos = 0;
        int ingresados = 0;
        int vendidosIniciados = 0;
        int incidencias = 0;
        BigDecimal ingresos = BigDecimal.ZERO;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int a = rs.getInt("asientos");
                    int v = rs.getInt("vendidos");
                    int ing = rs.getInt("ingresados");
                    int inc = rs.getInt("incidencias");
                    BigDecimal dinero = rs.getBigDecimal("ingresos");
                    // La asistencia solo se calcula en eventos que ya empezaron.
                    EstadoEvento estado = EstadoEvento.desdeBd(rs.getString("estado"));
                    boolean yaInicio = estado == EstadoEvento.EN_CURSO || estado == EstadoEvento.FINALIZADO;
                    asientos += a;
                    vendidos += v;
                    incidencias += inc;
                    ingresos = ingresos.add(dinero);
                    if (yaInicio) {
                        ingresados += ing;
                        vendidosIniciados += v;
                    }
                    r.agregarFila(rs.getString("nombre"),
                            Formatos.fecha(rs.getObject("fecha_inicio", LocalDateTime.class)),
                            rs.getString("estado"), rs.getString("recinto"), a, v, a - v,
                            Formatos.porcentaje(v, a) + "%", Formatos.dinero(dinero), ing,
                            yaInicio ? Formatos.porcentaje(ing, v) + "%" : "—", inc);
                }
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar el reporte general", ex);
        }
        r.setTotales("Total", "", "", "", asientos, vendidos, asientos - vendidos,
                Formatos.porcentaje(vendidos, asientos) + "%", Formatos.dinero(ingresos), ingresados,
                Formatos.porcentaje(ingresados, vendidosIniciados) + "%", incidencias);
        return r;
    }

    /**
     * Con un evento elegido: toda su información en filas Sección / Dato /
     * Valor (datos, asientos, ventas, precios por zona, asistencia e
     * incidencias). Así cabe en la misma tabla, el CSV y el PDF.
     */
    private Reporte detalleEvento(long idEvento) {
        String sqlEvento = "SELECT ev.nombre, ev.tipo, ev.participantes, ev.apertura_puertas, ev.fecha_inicio, "
                + "ev.fecha_fin, ev.estado, c.nombre AS categoria, r.nombre AS recinto "
                + "FROM evento ev LEFT JOIN categoria c ON c.id = ev.id_categoria "
                + "JOIN recinto r ON r.id = ev.id_recinto WHERE ev.id = ? AND ev.activo = 1";
        String sqlAsientos = "SELECT COUNT(*) AS total, "
                + "SUM(estado = 'Vendido') AS vendidos, SUM(estado = 'Reservado') AS reservados, "
                + "SUM(estado = 'Bloqueado') AS bloqueados, SUM(estado = 'Disponible') AS disponibles "
                + "FROM evento_asiento WHERE id_evento = ?";
        String sqlVentas = "SELECT COUNT(DISTINCT v.id) AS ventas, COUNT(*) AS boletos, "
                + "COALESCE(SUM(en.precio), 0) AS ingresos, "
                + "COALESCE(SUM(CASE WHEN v.canal = 'Web' THEN en.precio END), 0) AS web, "
                + "COALESCE(SUM(CASE WHEN v.canal = 'Mostrador' THEN en.precio END), 0) AS mostrador "
                + "FROM entrada en JOIN venta v ON v.id = en.id_venta "
                + "JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "WHERE ea.id_evento = ? AND en.estado <> 'Anulada'";
        String sqlAnulados = "SELECT COUNT(*) FROM entrada en JOIN evento_asiento ea ON ea.id = en.id_evento_asiento "
                + "WHERE ea.id_evento = ? AND en.estado = 'Anulada'";
        String sqlZonas = "SELECT o.zona, o.total_asientos, o.vendidos, pz.precio "
                + "FROM v_ocupacion_evento_zona o "
                + "LEFT JOIN precio_zona pz ON pz.id_evento = o.id_evento AND pz.id_zona = o.id_zona "
                + "WHERE o.id_evento = ? ORDER BY o.id_zona";
        String sqlAsistencia = "SELECT "
                + "(SELECT COUNT(DISTINCT id_entrada) FROM acceso WHERE id_evento = ? AND resultado = 'Valido' AND anulado = 0) AS ingresados, "
                + "(SELECT COUNT(*) FROM acceso WHERE id_evento = ? AND resultado = 'Rechazado') AS rechazos";
        String sqlIncidencias = "SELECT COUNT(*) AS total, SUM(estado = 'Abierta') AS abiertas, "
                + "SUM(estado = 'En atencion') AS atencion, SUM(estado = 'Cerrada') AS cerradas, "
                + "SUM(gravedad = 'Alta') AS altas FROM incidencia WHERE id_evento = ?";

        try (Connection cn = ConexionBD.obtenerConexion()) {
            Reporte r;
            boolean yaInicio;
            try (PreparedStatement ps = cn.prepareStatement(sqlEvento)) {
                ps.setLong(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        r = new Reporte("Reporte general", "Sección", "Dato", "Valor");
                        return r;
                    }
                    r = new Reporte("Reporte general: " + rs.getString("nombre"), "Sección", "Dato", "Valor");
                    TipoEvento tipo = TipoEvento.desdeBd(rs.getString("tipo"));
                    r.agregarFila("Evento", "Nombre", rs.getString("nombre"));
                    r.agregarFila("Evento", "Tipo", tipo == null ? "" : tipo.getEtiqueta());
                    r.agregarFila("Evento", "Categoría", rs.getString("categoria") == null ? "Sin categoría" : rs.getString("categoria"));
                    r.agregarFila("Evento", "Participantes", rs.getString("participantes"));
                    r.agregarFila("Evento", "Recinto", rs.getString("recinto"));
                    r.agregarFila("Evento", "Apertura de puertas",
                            Formatos.fechaHora(rs.getObject("apertura_puertas", LocalDateTime.class)));
                    r.agregarFila("Evento", "Inicio", Formatos.fechaHora(rs.getObject("fecha_inicio", LocalDateTime.class)));
                    r.agregarFila("Evento", "Fin", Formatos.fechaHora(rs.getObject("fecha_fin", LocalDateTime.class)));
                    r.agregarFila("Evento", "Estado", rs.getString("estado"));
                    // La asistencia solo tiene sentido si el evento ya empezó.
                    EstadoEvento estado = EstadoEvento.desdeBd(rs.getString("estado"));
                    yaInicio = estado == EstadoEvento.EN_CURSO || estado == EstadoEvento.FINALIZADO;
                }
            }

            int total;
            int vendidos;
            try (PreparedStatement ps = cn.prepareStatement(sqlAsientos)) {
                ps.setLong(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    total = rs.getInt("total");
                    vendidos = rs.getInt("vendidos");
                    r.agregarFila("Asientos", "Total de asientos", total);
                    r.agregarFila("Asientos", "Vendidos", vendidos);
                    r.agregarFila("Asientos", "Sin vender", total - vendidos);
                    r.agregarFila("Asientos", "Disponibles", rs.getInt("disponibles"));
                    r.agregarFila("Asientos", "Reservados", rs.getInt("reservados"));
                    r.agregarFila("Asientos", "Bloqueados", rs.getInt("bloqueados"));
                    r.agregarFila("Asientos", "% ocupación", Formatos.porcentaje(vendidos, total) + "%");
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlVentas)) {
                ps.setLong(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int boletos = rs.getInt("boletos");
                    BigDecimal ingresos = rs.getBigDecimal("ingresos");
                    r.agregarFila("Ventas", "Número de ventas", rs.getInt("ventas"));
                    r.agregarFila("Ventas", "Boletos vendidos", boletos);
                    r.agregarFila("Ventas", "Ingresos totales", Formatos.dinero(ingresos));
                    r.agregarFila("Ventas", "Ingresos por canal Web", Formatos.dinero(rs.getBigDecimal("web")));
                    r.agregarFila("Ventas", "Ingresos por Mostrador", Formatos.dinero(rs.getBigDecimal("mostrador")));
                    r.agregarFila("Ventas", "Precio promedio por boleto", boletos == 0 ? Formatos.dinero(BigDecimal.ZERO)
                            : Formatos.dinero(ingresos.divide(BigDecimal.valueOf(boletos), 2, java.math.RoundingMode.HALF_UP)));
                }
            }
            try (PreparedStatement ps = cn.prepareStatement(sqlAnulados)) {
                ps.setLong(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    r.agregarFila("Ventas", "Boletos anulados", rs.getInt(1));
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlZonas)) {
                ps.setLong(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BigDecimal precio = rs.getBigDecimal("precio");
                        r.agregarFila("Zonas", rs.getString("zona"),
                                rs.getInt("vendidos") + " de " + rs.getInt("total_asientos") + " vendidos · "
                                + (precio == null ? "sin precio" : Formatos.dinero(precio)));
                    }
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlAsistencia)) {
                ps.setLong(1, idEvento);
                ps.setLong(2, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int ingresados = rs.getInt("ingresados");
                    r.agregarFila("Asistencia", "Ingresaron", ingresados);
                    r.agregarFila("Asistencia", "Ausentes",
                            yaInicio ? String.valueOf(Math.max(0, vendidos - ingresados)) : "Aún no inicia");
                    r.agregarFila("Asistencia", "% asistencia",
                            yaInicio ? Formatos.porcentaje(ingresados, vendidos) + "%" : "Aún no inicia");
                    r.agregarFila("Asistencia", "Rechazos en puerta", rs.getInt("rechazos"));
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlIncidencias)) {
                ps.setLong(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    r.agregarFila("Incidencias", "Total", rs.getInt("total"));
                    r.agregarFila("Incidencias", "Abiertas", rs.getInt("abiertas"));
                    r.agregarFila("Incidencias", "En atención", rs.getInt("atencion"));
                    r.agregarFila("Incidencias", "Cerradas", rs.getInt("cerradas"));
                    r.agregarFila("Incidencias", "De gravedad alta", rs.getInt("altas"));
                }
            }
            return r;
        } catch (SQLException ex) {
            throw new DAOException("No se pudo generar el reporte general del evento", ex);
        }
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
