package sv.edu.itca.ticka.dao;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Incidencia;

/** DAO de incidencias (M5, RF-32 a RF-35). */
public interface IncidenciaDAO {

    long insertar(Incidencia incidencia);

    /** Igual que insertar, dentro de una transacción (incidencia automática por rechazo, RF-35). */
    long insertar(Connection cn, Incidencia incidencia);

    /** Incidencias filtradas (cada criterio null = todos), de la más reciente a la más antigua (RF-34). */
    List<Incidencia> listar(Long idEvento, String tipo, String gravedad, String estado, int limite);

    Optional<Incidencia> buscarPorId(long id);

    /** Seguimiento (RF-33): nuevo estado y observaciones. */
    void actualizarEstado(long id, String estado, String observaciones);
}
