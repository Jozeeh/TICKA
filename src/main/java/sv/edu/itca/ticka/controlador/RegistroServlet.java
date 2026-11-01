package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.UsuarioDAOImpl;
import sv.edu.itca.ticka.filtro.AutenticacionFilter;
import sv.edu.itca.ticka.filtro.CsrfFilter;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.Usuario;
import sv.edu.itca.ticka.servicio.ClienteServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Autorregistro del cliente (RF-13, RF-14). Ruta pública: crea la cuenta
 * con rol Cliente y deja la sesión iniciada; si el cliente venía de elegir
 * un evento, vuelve a esa página.
 */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/registro.jsp";

    private UsuarioDAO usuarioDAO;
    private ClienteServicio clienteServicio;

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAOImpl();
        clienteServicio = new ClienteServicio(new ClienteDAOImpl(), usuarioDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Sesion.usuario(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/panel");
            return;
        }
        req.setAttribute("cliente", new Cliente());
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Cliente c = new Cliente();
        c.setNombres(req.getParameter("nombres"));
        c.setApellidos(req.getParameter("apellidos"));
        c.setDocumento(req.getParameter("documento"));
        c.setCorreo(req.getParameter("correo"));
        c.setTelefono(req.getParameter("telefono"));
        String username = req.getParameter("usuario");
        try {
            Usuario u = clienteServicio.registrar(username, req.getParameter("clave"),
                    req.getParameter("confirmacion"), c);
            Usuario sesionUsuario = usuarioDAO.buscarPorId(u.getId()).orElse(u);

            // Nueva sesión con el cliente autenticado (contra fijación de sesión).
            HttpSession anterior = req.getSession(false);
            String destino = null;
            if (anterior != null) {
                destino = (String) anterior.getAttribute(AutenticacionFilter.DESTINO);
                anterior.invalidate();
            }
            HttpSession sesion = req.getSession(true);
            sesion.setAttribute(AutenticacionFilter.USUARIO_SESION, sesionUsuario);
            sesion.removeAttribute(CsrfFilter.ATRIBUTO);
            Sesion.exito(req, "¡Bienvenido, " + c.getNombres() + "! Su cuenta se creó correctamente.");
            boolean destinoValido = destino != null && destino.startsWith(req.getContextPath() + "/")
                    && !destino.startsWith("//");
            resp.sendRedirect(destinoValido ? destino : req.getContextPath() + "/#cartelera");
        } catch (ServicioException ex) {
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("cliente", c);
            req.setAttribute("usuario", username);
            req.getRequestDispatcher(VISTA).forward(req, resp);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo crear la cuenta. Intente de nuevo.");
            req.setAttribute("cliente", c);
            req.setAttribute("usuario", username);
            req.getRequestDispatcher(VISTA).forward(req, resp);
        }
    }
}
