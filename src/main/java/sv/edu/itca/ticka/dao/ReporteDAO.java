package sv.edu.itca.ticka.dao;

import java.time.LocalDate;
import java.util.List;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.Indicadores;
import sv.edu.itca.ticka.modelo.Reporte;

/**
 * Consultas agregadas para el dashboard y los reportes del Administrador
 * (RF-42 a RF-48). Los filtros desde/hasta se aplican sobre la fecha de
 * inicio del evento; idEvento null significa "todos".
 */
public interface ReporteDAO {

    Indicadores indicadores();

    /** Próximos eventos a la venta o en curso, con sus asientos vendidos. */
    List<Evento> proximosEventos(int limite);

    /** Eventos que ya tienen mapa (para el filtro de los reportes). */
    List<Evento> eventosConMapa();

    Reporte ventas(LocalDate desde, LocalDate hasta, Long idEvento);

    Reporte asistencia(LocalDate desde, LocalDate hasta, Long idEvento);

    Reporte ocupacion(LocalDate desde, LocalDate hasta, Long idEvento);

    Reporte incidencias(LocalDate desde, LocalDate hasta, Long idEvento);
}
