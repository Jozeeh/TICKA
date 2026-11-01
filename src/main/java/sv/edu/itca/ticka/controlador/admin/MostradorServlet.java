package sv.edu.itca.ticka.controlador.admin;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.AsientoEventoDAO;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.impl.AsientoEventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.VentaDAOImpl;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroEvento;
import sv.edu.itca.ticka.servicio.CompraServicio;
import sv.edu.itca.ticka.servicio.MapaAsientos;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Configuracion;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Venta en mostrador (RF-24): el Administrador elige el evento, busca al
 * cliente por su documento, marca los asientos en el mismo mapa del portal
 * y registra la venta (canal Mostrador, con él como vendedor).
 */
@WebServlet("/admin/mostrador")
public class MostradorServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/admin/mostrador.jsp";

    private EventoDAO eventoDAO;
    private AsientoEventoDAO asientoEventoDAO;
    private ClienteDAO clienteDAO;
    private CompraServicio compraServicio;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        asientoEventoDAO = new AsientoEventoDAOImpl();
        clienteDAO = new ClienteDAOImpl();
        compraServicio = new CompraServicio(eventoDAO, new VentaDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        String documento = Parametros.texto(req.getParameter("documento"));
        try {
            // Solo eventos a la venta y que todavía no empiezan (RN-03).
            LocalDateTime ahora = LocalDateTime.now();
            List<Evento> eventos = eventoDAO.listarCartelera(new FiltroEvento()).stream()
                    .filter(e -> e.getEstado() == EstadoEvento.PUBLICADO && e.getFechaInicio().isAfter(ahora))
                    .collect(Collectors.toList());
            req.setAttribute("eventos", eventos);

            Cliente cliente = null;
            if (documento != null) {
                cliente = clienteDAO.buscarPorDocumento(documento.toUpperCase()).orElse(null);
                if (cliente == null) {
                    req.setAttribute("error", "No hay un cliente con ese documento. El cliente debe crear su cuenta en el portal.");
                } else if (!cliente.isActivo()) {
                    req.setAttribute("error", "La cuenta de ese cliente está desactivada.");
                    cliente = null;
                }
            }
            Evento evento = idEvento == null ? null
                    : eventos.stream().filter(e -> e.getId().equals(idEvento)).findFirst().orElse(null);
            if (evento != null && cliente != null) {
                List<Asiento> asientos = asientoEventoDAO.listarMapa(evento.getId());
                req.setAttribute("zonas", MapaAsientos.armarZonas(
                        asientoEventoDAO.listarPrecios(evento.getId(), evento.getIdRecinto()), asientos));
                req.setAttribute("maximo", Configuracion.maximoPorCompra());
                req.setAttribute("formasPago", new String[]{"Efectivo", "Tarjeta", "Transferencia"});
            }
            req.setAttribute("evento", evento);
            req.setAttribute("cliente", cliente);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar la venta en mostrador.");
            req.setAttribute("eventos", Collections.emptyList());
        }
        req.setAttribute("idEvento", idEvento);
        req.setAttribute("documento", documento);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        Long idCliente = Parametros.enteroLargo(req.getParameter("cliente"));
        if (idEvento == null || idCliente == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Cliente cliente = clienteDAO.buscarPorId(idCliente)
                    .filter(Cliente::isActivo)
                    .orElseThrow(() -> new ServicioException("El cliente no existe o está desactivado."));
            long idVenta = compraServicio.venderMostrador(idEvento, cliente.getId(),
                    Parametros.ids(req.getParameterValues("asiento")),
                    req.getParameter("formaPago"), Sesion.usuario(req).getId());
            Sesion.exito(req, "Venta #" + idVenta + " registrada en mostrador a nombre de " + cliente.getNombreCompleto()
                    + ". El cliente ve sus boletos con QR en su cuenta.");
            resp.sendRedirect(req.getContextPath() + "/admin/clientes?accion=ver&id=" + cliente.getId());
            return;
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo registrar la venta.");
        }
        String documento = clienteDAO.buscarPorId(idCliente).map(Cliente::getDocumento).orElse("");
        resp.sendRedirect(req.getContextPath() + "/admin/mostrador?evento=" + idEvento + "&documento="
                + URLEncoder.encode(documento, StandardCharsets.UTF_8));
    }
}
