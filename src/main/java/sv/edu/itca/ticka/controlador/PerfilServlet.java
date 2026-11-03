package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.dao.impl.UsuarioDAOImpl;
import sv.edu.itca.ticka.modelo.Usuario;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.servicio.UsuarioServicio;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Mi perfil del personal (Administrador, Organizador y Control de acceso):
 * cambiar su nombre y su contraseña. El usuario y el rol solo los cambia un
 * administrador. El cliente tiene su propio perfil en /cliente/perfil.
 */
@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/perfil.jsp";

    private UsuarioDAO usuarioDAO;
    private UsuarioServicio usuarioServicio;

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAOImpl();
        usuarioServicio = new UsuarioServicio(usuarioDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario usuario = Sesion.usuario(req);
        if (usuario.isCliente()) {
            resp.sendRedirect(req.getContextPath() + "/cliente/perfil");
            return;
        }
        try {
            req.setAttribute("cuenta", usuarioDAO.buscarPorId(usuario.getId()).orElse(null));
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar su perfil.");
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario usuario = Sesion.usuario(req);
        if (usuario.isCliente()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            if ("clave".equals(req.getParameter("accion"))) {
                usuarioServicio.cambiarClavePropia(usuario.getId(), req.getParameter("actual"),
                        req.getParameter("nueva"), req.getParameter("confirmacion"));
                Sesion.exito(req, "Contraseña actualizada.");
            } else {
                usuarioServicio.actualizarNombrePropio(usuario.getId(), req.getParameter("nombre"));
                usuario.setNombre(req.getParameter("nombre").trim());
                Sesion.exito(req, "Datos actualizados.");
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo guardar los cambios.");
        }
        resp.sendRedirect(req.getContextPath() + "/perfil");
    }
}
