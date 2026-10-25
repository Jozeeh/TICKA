package sv.edu.itca.ticka.filtro;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * Protege las rutas privadas en dos pasos:
 * 1) Autenticación: debe existir una sesión con usuarioAutenticado; si no,
 *    redirige a /acceso?estado=sesion.
 * 2) Autorización por rol (RF-38, matriz 2.2 de requerimientos):
 *    /admin/*        solo Administrador.
 *    /organizador/*  Organizador y Administrador.
 *    /reportes       Organizador y Administrador (solo consulta).
 *    Si el rol no corresponde responde 403 (página de error personalizada).
 */
public class AutenticacionFilter implements Filter {

    public static final String USUARIO_SESION = "usuarioAutenticado";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession sesion = request.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute(USUARIO_SESION);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/acceso?estado=sesion");
            return;
        }

        String ruta = request.getServletPath();
        if (!tienePermiso(usuario, ruta)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Páginas privadas: el navegador no debe guardarlas en caché, así el
        // botón "Atrás" no muestra datos después de cerrar sesión.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        chain.doFilter(req, res);
    }

    static boolean tienePermiso(Usuario usuario, String ruta) {
        if (ruta.startsWith("/admin")) {
            return usuario.isAdministrador();
        }
        if (ruta.startsWith("/organizador") || ruta.startsWith("/reportes")) {
            return usuario.isGestorEventos();
        }
        return true;
    }
}
