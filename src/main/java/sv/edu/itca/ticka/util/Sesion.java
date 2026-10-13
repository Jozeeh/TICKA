package sv.edu.itca.ticka.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import sv.edu.itca.ticka.filtro.AutenticacionFilter;
import sv.edu.itca.ticka.modelo.Usuario;

/** Atajos para leer la sesión y dejar mensajes flash entre redirecciones. */
public final class Sesion {

    private Sesion() {
    }

    /** Usuario autenticado (las rutas privadas siempre lo tienen por el filtro). */
    public static Usuario usuario(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (Usuario) s.getAttribute(AutenticacionFilter.USUARIO_SESION);
    }

    public static void exito(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("flash", mensaje);
    }

    public static void error(HttpServletRequest req, String mensaje) {
        req.getSession().setAttribute("flashError", mensaje);
    }
}
