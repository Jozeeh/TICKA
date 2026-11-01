package sv.edu.itca.ticka.controlador.cliente;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.UsuarioDAOImpl;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.Usuario;
import sv.edu.itca.ticka.servicio.ClienteServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Perfil del cliente (RF-13): editar sus datos (el documento no se cambia) y
 * cambiar su contraseña verificando la actual.
 */
@WebServlet("/cliente/perfil")
public class PerfilServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/perfil.jsp";

    private ClienteDAO clienteDAO;
    private UsuarioDAO usuarioDAO;
    private ClienteServicio clienteServicio;

    @Override
    public void init() {
        clienteDAO = new ClienteDAOImpl();
        usuarioDAO = new UsuarioDAOImpl();
        clienteServicio = new ClienteServicio(clienteDAO, usuarioDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("cliente", clienteDAO.buscarPorUsuario(Sesion.usuario(req).getId()).orElse(null));
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
        try {
            if ("clave".equals(req.getParameter("accion"))) {
                clienteServicio.cambiarClave(usuario.getId(), req.getParameter("actual"),
                        req.getParameter("nueva"), req.getParameter("confirmacion"));
                Sesion.exito(req, "Contraseña actualizada.");
            } else {
                Cliente c = clienteDAO.buscarPorUsuario(usuario.getId())
                        .orElseThrow(() -> new ServicioException("Su cuenta no tiene datos de cliente."));
                c.setNombres(req.getParameter("nombres"));
                c.setApellidos(req.getParameter("apellidos"));
                c.setCorreo(req.getParameter("correo"));
                c.setTelefono(req.getParameter("telefono"));
                try {
                    clienteServicio.actualizarPerfil(c);
                } catch (ServicioException ex) {
                    // Se vuelve a mostrar el formulario con lo que escribió.
                    req.setAttribute("error", ex.getMessage());
                    req.setAttribute("cliente", c);
                    req.getRequestDispatcher(VISTA).forward(req, resp);
                    return;
                }
                usuario.setNombre(c.getNombreCompleto());
                Sesion.exito(req, "Datos actualizados.");
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo guardar los cambios.");
        }
        resp.sendRedirect(req.getContextPath() + "/cliente/perfil");
    }
}
