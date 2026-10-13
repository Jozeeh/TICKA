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
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.servicio.UsuarioServicio;

/**
 * Crea el primer Administrador cuando la base no tiene ninguno activo. Así
 * el repositorio no necesita guardar usuarios ni contraseñas: cada integrante
 * crea su propio administrador local la primera vez que abre la aplicación.
 * Una vez creado, esta ruta redirige siempre al inicio de sesión.
 */
@WebServlet("/configuracion-inicial")
public class ConfiguracionInicialServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/acceso/configuracion-inicial.jsp";

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
        try {
            if (usuarioDAO.existeAdministradorActivo()) {
                resp.sendRedirect(req.getContextPath() + "/acceso");
                return;
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No hay conexión con la base de datos. Revise que MySQL esté activo y la configuración de db.properties.");
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String usuario = req.getParameter("usuario");
        String nombre = req.getParameter("nombre");
        try {
            usuarioServicio.crearAdministradorInicial(usuario, nombre,
                    req.getParameter("clave"), req.getParameter("confirmacion"));
            req.getSession().setAttribute("flash", "Administrador creado. Ya puede iniciar sesión.");
            resp.sendRedirect(req.getContextPath() + "/acceso");
        } catch (ServicioException | DAOException ex) {
            if (ex instanceof DAOException) {
                log("Error de persistencia", ex);
            }
            req.setAttribute("error", ex instanceof ServicioException ? ex.getMessage()
                    : "No se pudo guardar. Revise la conexión con la base de datos.");
            req.setAttribute("usuario", usuario);
            req.setAttribute("nombre", nombre);
            req.getRequestDispatcher(VISTA).forward(req, resp);
        }
    }
}
