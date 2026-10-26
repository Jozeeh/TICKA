package sv.edu.itca.ticka.controlador.admin;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.UsuarioDAOImpl;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.Paginacion;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.servicio.UsuarioServicio;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Clientes registrados (RF-37, RF-54, RF-55): el Administrador los busca y
 * pagina, consulta el detalle de cada uno con todas sus compras y boletos, y
 * puede activar o desactivar su cuenta. Las cuentas se crean por
 * autorregistro en el portal del cliente, no desde aquí.
 */
@WebServlet("/admin/clientes")
public class ClienteServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/admin/clientes.jsp";
    private static final String VISTA_DETALLE = "/WEB-INF/vistas/admin/cliente-detalle.jsp";

    private ClienteDAO clienteDAO;
    private UsuarioServicio usuarioServicio;

    @Override
    public void init() {
        clienteDAO = new ClienteDAOImpl();
        usuarioServicio = new UsuarioServicio(new UsuarioDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("ver".equals(req.getParameter("accion"))) {
            mostrarDetalle(req, resp);
            return;
        }
        String texto = Parametros.texto(req.getParameter("texto"));
        try {
            Paginacion pag = new Paginacion(Parametros.entero(req.getParameter("pagina")), clienteDAO.contar(texto));
            req.setAttribute("clientes", clienteDAO.listar(texto, pag.getDesplazamiento(), pag.getTamano()));
            req.setAttribute("paginacion", pag);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar los clientes. Verifique la conexión con MySQL.");
            req.setAttribute("clientes", Collections.emptyList());
        }
        req.setAttribute("texto", texto);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    private void mostrarDetalle(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        try {
            Cliente cliente = id == null ? null : clienteDAO.buscarPorId(id).orElse(null);
            if (cliente == null) {
                Sesion.error(req, "El cliente no existe.");
                resp.sendRedirect(req.getContextPath() + "/admin/clientes");
                return;
            }
            req.setAttribute("cliente", cliente);
            req.setAttribute("compras", clienteDAO.compras(cliente.getId()));
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar el cliente. Verifique la conexión con MySQL.");
            resp.sendRedirect(req.getContextPath() + "/admin/clientes");
            return;
        }
        req.getRequestDispatcher(VISTA_DETALLE).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idUsuario = Parametros.enteroLargo(req.getParameter("usuario"));
        boolean activar = "si".equals(req.getParameter("activar"));
        try {
            if (idUsuario == null) {
                throw new ServicioException("Cliente no válido.");
            }
            usuarioServicio.cambiarEstado(idUsuario, activar, Sesion.usuario(req).getId());
            Sesion.exito(req, activar ? "Cuenta del cliente activada." : "Cuenta del cliente desactivada: ya no podrá iniciar sesión.");
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo actualizar la cuenta.");
        }
        // Si la acción se hizo desde el detalle, se vuelve al detalle del mismo cliente.
        Long idCliente = Parametros.enteroLargo(req.getParameter("cliente"));
        resp.sendRedirect(req.getContextPath() + "/admin/clientes"
                + (idCliente == null ? "" : "?accion=ver&id=" + idCliente));
    }
}
