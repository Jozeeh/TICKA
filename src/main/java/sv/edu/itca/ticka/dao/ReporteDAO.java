package sv.edu.itca.ticka.dao;

import java.util.List;
import java.util.Map;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroReporte;
import sv.edu.itca.ticka.modelo.Indicadores;
import sv.edu.itca.ticka.modelo.Reporte;

/**
 * Consultas agregadas para el dashboard y los reportes del Administrador
 * (RF-42 a RF-48). Las fechas del filtro se aplican sobre la fecha de
 * inicio del evento; cada criterio null significa "todos".
 */
public interface ReporteDAO {

    Indicadores indicadores();

    /** Próximos eventos a la venta o en curso, con sus asientos vendidos. */
    List<Evento> proximosEventos(int limite);

    /** Eventos que ya tienen mapa (para el filtro de los reportes). */
    List<Evento> eventosConMapa();

    Reporte ventas(FiltroReporte filtro);

    Reporte asistencia(FiltroReporte filtro);

    Reporte ocupacion(FiltroReporte filtro);

    /** El filtro de zona no aplica: las incidencias no tienen zona. */
    Reporte incidencias(FiltroReporte filtro);

    /** Zonas de todos los recintos ("Recinto · Zona") para el filtro de zona. */
    Map<Long, String> zonasParaFiltro();
}
