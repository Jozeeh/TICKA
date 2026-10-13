package sv.edu.itca.ticka.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Recinto;

/** DAO de la tabla recinto (RF-10). */
public interface RecintoDAO {

    /** Todos los recintos con su cantidad de zonas y asientos activos. */
    List<Recinto> listar();

    /** Solo recintos activos (para elegir el recinto de un evento). */
    List<Recinto> listarActivos();

    Optional<Recinto> buscarPorId(long id);

    boolean existeNombre(String nombre, Long excluirId);

    long insertar(Recinto recinto);

    boolean actualizar(Recinto recinto);
}
