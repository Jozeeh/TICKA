package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.dao.impl.UsuarioDAOImpl;
import sv.edu.itca.ticka.filtro.AutenticacionFilter;
import sv.edu.itca.ticka.filtro.CsrfFilter;
import sv.edu.itca.ticka.modelo.Usuario;
import sv.edu.itca.ticka.servicio.AutenticacionServicio;
import sv.edu.itca.ticka.servicio.ServicioException;

/**
 * Inicio de sesión (RF-37). La validación de credenciales y el bloqueo por
 * intentos se delegan a AutenticacionServicio. Esta ruta no está protegida
 * por AutenticacionFilter porque un usuario anónimo necesita llegar aquí.
 *
 * Si la base todavía no tiene un Administrador activo, redirige a la
 * configuración inicial para crearlo.
 */
@WebServlet("/acceso")
public class AccesoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/acceso/login.jsp";
    private static final String COOKIE_USUARIO = "ultimoUsuario";
    private static final Pattern USERNAME_SEGURO = Pattern.compile("^[a-z0-9._-]{1,30}$");

    private UsuarioDAO usuarioDAO;
    private AutenticacionServicio autenticacion;

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAOImpl();
        autenticacion = new AutenticacionServicio(usuarioDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null && sesion.getAttribute(AutenticacionFilter.USUARIO_SESION) != null) {
            resp.sendRedirect(req.getContextPath() + "/panel");
            return;
        }
        try {
            if (!usuarioDAO.existeAdministradorActivo()) {
                resp.sendRedirect(req.getContextPath() + "/configuracion-inicial");
                return;
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No hay conexión con la base de datos. Revise la página de diagnóstico.");
        }

        String estado = req.getParameter("estado");
        if ("cerrada".equals(estado)) {
            req.setAttribute("mensajeExito", "La sesión se cerró correctamente.");
        } else if ("sesion".equals(estado)) {
            req.setAttribute("mensajeAviso", "Inicie sesión para continuar.");
        } else if ("expirada".equals(estado)) {
            req.setAttribute("mensajeAviso", "El formulario venció. Escriba sus datos de nuevo.");
        }

        String ultimo = buscarCookie(req, COOKIE_USUARIO);
        if (ultimo != null && USERNAME_SEGURO.matcher(ultimo).matches()) {
            req.setAttribute("ultimoUsuario", ultimo);
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("usuario");
        String clave = req.getParameter("clave");

        Usuario usuario;
        try {
            usuario = autenticacion.autenticar(username, clave);
        } catch (ServicioException ex) {
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("ultimoUsuario", username);
            req.getRequestDispatcher(VISTA).forward(req, resp);
            return;
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No hay conexión con la base de datos. Intente más tarde.");
            req.getRequestDispatcher(VISTA).forward(req, resp);
            return;
        }

        // Se descarta la sesión anónima para evitar fijación de sesión.
        HttpSession anterior = req.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = req.getSession(true);
        sesion.setAttribute(AutenticacionFilter.USUARIO_SESION, usuario);
        sesion.removeAttribute(CsrfFilter.ATRIBUTO);

        if ("si".equals(req.getParameter("recordar"))) {
            agregarCookie(req, resp, usuario.getUsername(), 7 * 24 * 60 * 60);
        } else {
            agregarCookie(req, resp, "", 0);
        }
        resp.sendRedirect(req.getContextPath() + "/panel");
    }

    /** Cookie "recordar usuario" (RF-42): solo guarda el username, nunca la contraseña. */
    private void agregarCookie(HttpServletRequest req, HttpServletResponse resp, String valor, int segundos) {
        Cookie cookie = new Cookie(COOKIE_USUARIO, valor);
        cookie.setMaxAge(segundos);
        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure());
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        resp.addCookie(cookie);
    }

    private String buscarCookie(HttpServletRequest req, String nombre) {
        if (req.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : req.getCookies()) {
            if (nombre.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
