package sv.edu.itca.ticka.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * DAO de la tabla usuario (con el nombre del rol tomado de la tabla rol).
 */
public interface UsuarioDAO {

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorId(long id);

    /** Personal interno: todos los usuarios cuyo rol no es Cliente. */
    List<Usuario> listarPersonal();

    /** true si existe al menos un Administrador activo. */
    boolean existeAdministradorActivo();

    /** Cantidad de Administradores activos (para no desactivar al último). */
    int contarAdministradoresActivos();

    /** true si otro usuario (distinto de excluirId) ya usa ese username. */
    boolean existeUsername(String username, Long excluirId);

    long insertar(Usuario usuario);

    /** Actualiza nombre, rol y estado (no toca la contraseña). */
    boolean actualizar(Usuario usuario);

    boolean cambiarClave(long id, String passwordHash);

    boolean cambiarEstado(long id, boolean activo);

    void registrarIntentoFallido(long id, int intentos, LocalDateTime bloqueadoHasta);

    /** Limpia intentos fallidos y bloqueo (login correcto o desbloqueo manual). */
    void reiniciarIntentos(long id);
}
