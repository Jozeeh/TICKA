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
 *    /cliente/*      solo Cliente (portal del cliente, M3).
 *    /puerta/*       Control de acceso y Administrador (M4, M5).
 *    Si el rol no corresponde responde 403 (página de error personalizada).
 */
public class AutenticacionFilter implements Filter {

    public static final String USUARIO_SESION = "usuarioAutenticado";
    /** Página privada que se pidió sin sesión; AccesoServlet vuelve a ella al entrar. */
    public static final String DESTINO = "destinoTrasAcceso";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession sesion = request.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute(USUARIO_SESION);
        if (usuario == null) {
            // Se recuerda la página pedida (solo GET) para volver a ella después de iniciar sesión.
            if ("GET".equals(request.getMethod())) {
                String destino = request.getRequestURI()
                        + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
                request.getSession(true).setAttribute(DESTINO, destino);
            }
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
        if (ruta.startsWith("/cliente")) {
            return usuario.isCliente();
        }
        if (ruta.startsWith("/puerta")) {
            return usuario.isPersonalPuerta();
        }
        return true;
    }
}
