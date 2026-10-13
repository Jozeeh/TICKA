package sv.edu.itca.ticka.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Categoria;

/** DAO de la tabla categoria (RF-04). */
public interface CategoriaDAO {

    /** Todas las categorías con la cantidad de eventos que las usan. */
    List<Categoria> listar();

    Optional<Categoria> buscarPorId(long id);

    /** true si otra categoría (distinta de excluirId) ya usa ese nombre. */
    boolean existeNombre(String nombre, Long excluirId);

    long insertar(Categoria categoria);

    boolean actualizar(Categoria categoria);

    /** Elimina solo si ningún evento la usa; devuelve false si no se pudo. */
    boolean eliminar(long id);
}
