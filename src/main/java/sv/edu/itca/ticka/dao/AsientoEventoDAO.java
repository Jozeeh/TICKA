package sv.edu.itca.ticka.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.OcupacionZona;
import sv.edu.itca.ticka.modelo.PrecioZona;

/**
 * DAO de precios por zona (precio_zona) y de la disponibilidad de asientos
 * de cada evento (evento_asiento). Es la parte del modelo que garantiza
 * "un asiento = un boleto" (RN-01).
 */
public interface AsientoEventoDAO {

    /** Zonas del recinto del evento con su precio (null si aún no tiene). */
    List<PrecioZona> listarPrecios(long idEvento, long idRecinto);

    /** Inserta o actualiza el precio de cada zona (RF-05) en una transacción. */
    void guardarPrecios(long idEvento, Map<Long, BigDecimal> precios);

    /** Asientos del evento con su estado, ordenados por zona, fila y número. */
    List<Asiento> listarMapa(long idEvento);

    /** Conteo por estado de cada zona (vista v_ocupacion_evento_zona). */
    List<OcupacionZona> ocupacionPorEvento(long idEvento);

    /**
     * Pasa un asiento de Disponible a Bloqueado o al revés (RF-08). Devuelve
     * false si el asiento ya no estaba en el estado esperado.
     */
    boolean cambiarBloqueo(Connection cn, long idEvento, long idEventoAsiento, boolean bloquear);

    // ---- Operaciones dentro de una transacción ----

    /** Cantidad de asientos activos del recinto (para poder publicar). */
    int contarAsientosActivos(Connection cn, long idRecinto);

    /** Zonas con asientos activos que todavía no tienen precio en el evento. */
    int contarZonasSinPrecio(Connection cn, long idEvento, long idRecinto);

    /** Copia los asientos activos del recinto al evento como Disponible (RF-07). */
    int generarDisponibilidad(Connection cn, long idEvento, long idRecinto);

    /** Asientos que todavía se pueden vender (Disponible o reserva vencida). */
    int contarLibres(Connection cn, long idEvento);
}
