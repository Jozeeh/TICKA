package sv.edu.itca.ticka.servicio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * Valida credenciales y aplica el bloqueo temporal por intentos fallidos
 * (RF-40): al llegar a MAX_INTENTOS la cuenta queda bloqueada
 * MINUTOS_BLOQUEO minutos.
 */
public class AutenticacionServicio {

    public static final int MAX_INTENTOS = 5;
    public static final int MINUTOS_BLOQUEO = 15;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final UsuarioDAO usuarioDAO;

    public AutenticacionServicio(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Devuelve el usuario autenticado (copia sin hash) o lanza
     * ServicioException con el motivo que verá la persona.
     */
    public Usuario autenticar(String username, String clave) {
        if (username == null || username.isBlank() || clave == null || clave.isEmpty()) {
            throw new ServicioException("Ingrese su usuario y contraseña.");
        }
        Optional<Usuario> encontrado = usuarioDAO.buscarPorUsername(username.trim().toLowerCase());
        if (encontrado.isEmpty()) {
            throw new ServicioException("Usuario o contraseña incorrectos.");
        }
        Usuario u = encontrado.get();
        if (!u.isActivo()) {
            throw new ServicioException("La cuenta está desactivada. Contacte al administrador.");
        }
        if (u.isBloqueado()) {
            throw new ServicioException("Cuenta bloqueada por intentos fallidos. Intente de nuevo después de las "
                    + u.getBloqueadoHasta().format(HORA) + ".");
        }
        if (!SeguridadClave.verificar(clave, u.getPasswordHash())) {
            registrarFallo(u);
        }
        usuarioDAO.reiniciarIntentos(u.getId());
        return u.copiaParaSesion();
    }

    private void registrarFallo(Usuario u) {
        int intentos = u.getIntentosFallidos() + 1;
        if (intentos >= MAX_INTENTOS) {
            LocalDateTime hasta = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
            usuarioDAO.registrarIntentoFallido(u.getId(), 0, hasta);
            throw new ServicioException("Demasiados intentos fallidos. La cuenta quedó bloqueada "
                    + MINUTOS_BLOQUEO + " minutos.");
        }
        usuarioDAO.registrarIntentoFallido(u.getId(), intentos, null);
        int restantes = MAX_INTENTOS - intentos;
        throw new ServicioException("Usuario o contraseña incorrectos. Le quedan "
                + restantes + (restantes == 1 ? " intento." : " intentos."));
    }
}
