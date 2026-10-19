package sv.edu.itca.ticka.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.Zona;

/** DAO de las tablas zona y asiento (RF-11, RF-12). */
public interface ZonaDAO {

    /** Zonas de un recinto con el conteo de asientos. */
    List<Zona> listarPorRecinto(long idRecinto);

    Optional<Zona> buscarPorId(long id);

    boolean existeNombre(long idRecinto, String nombre, Long excluirId);

    long insertar(Zona zona);

    boolean actualizar(Zona zona);

    /** Elimina la zona solo si no tiene asientos. */
    boolean eliminar(long id);

    /** Asientos de todas las zonas del recinto, ordenados por zona, fila y número. */
    List<Asiento> listarAsientosPorRecinto(long idRecinto);

    /**
     * Aplica la distribución filas × porFila a la zona en una transacción:
     * crea los asientos que falten, activa los que están dentro de la
     * cuadrícula e inactiva (sin borrar, RN-14) los que quedaron fuera.
     * Devuelve {creados, activados, inactivados}.
     */
    int[] configurarAsientos(long idZona, List<String> filas, int porFila);

    /** Habilita o inhabilita un asiento de la plantilla (RN-14). */
    boolean cambiarActivoAsiento(long idAsiento, boolean activo);
}
