package sv.edu.itca.ticka.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * DAO de la tabla usuario (con el nombre del rol tomado de la tabla rol).
 */
public interface UsuarioDAO {

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorId(long id);

    /**
     * Página del personal interno (rol distinto de Cliente). texto busca en
     * nombre y usuario; rol filtra por un rol. Ambos son opcionales (null).
     */
    List<Usuario> listarPersonal(String texto, Rol rol, int desplazamiento, int limite);

    int contarPersonal(String texto, Rol rol);

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
