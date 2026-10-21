package sv.edu.itca.ticka.dao;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Afiche;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroEvento;
import sv.edu.itca.ticka.modelo.TipoEvento;

/**
 * DAO de la tabla evento (RF-01 a RF-03). Los métodos que reciben una
 * Connection participan en una transacción abierta por EventoServicio
 * (publicar y cancelar), igual que en la guía de transacciones.
 */
public interface EventoDAO {

    /** Página de eventos activos según el filtro, del más reciente al más antiguo. */
    List<Evento> listar(FiltroEvento filtro);

    int contar(FiltroEvento filtro);

    Optional<Evento> buscarPorId(long id);

    long insertar(Evento evento);

    boolean actualizar(Evento evento);

    /**
     * RN-05: true si el recinto ya tiene otro evento activo y no cancelado
     * cuyo horario se traslapa con [inicio, fin).
     */
    boolean existeChoque(long idRecinto, LocalDateTime inicio, LocalDateTime fin, Long excluirId);

    /** true si el evento tiene boletos vigentes (no anulados). */
    boolean tieneVentas(long idEvento);

    /**
     * Cartelera pública: eventos publicados, agotados o en curso que todavía
     * no terminan, con precio mínimo, asientos libres y precios por zona.
     * Si tipo no es null, filtra por tipo de evento.
     */
    List<Evento> listarCartelera(TipoEvento tipo);

    /** Guarda (o reemplaza) la imagen adjunta del evento. */
    void guardarAfiche(long idEvento, byte[] contenido, String tipo);

    void quitarAfiche(long idEvento);

    /** Copia el afiche de un evento a otro (duplicar evento, RF-09). */
    void copiarAfiche(long idOrigen, long idDestino);

    /** Borrado lógico (RF-01, RN-10): el evento deja de aparecer en listados y reportes. */
    boolean desactivar(long idEvento);

    /** Imagen adjunta del evento, si tiene. */
    Optional<Afiche> obtenerAfiche(long idEvento);

    // ---- Operaciones dentro de una transacción ----

    /** Lee el evento bloqueando su fila (SELECT ... FOR UPDATE). */
    Optional<Evento> buscarParaActualizar(Connection cn, long id);

    void cambiarEstado(Connection cn, long id, EstadoEvento estado);

    /** Anula boletos y ventas del evento (cancelación, RN-09). Devuelve boletos anulados. */
    int anularVentas(Connection cn, long idEvento, String motivo);
}
