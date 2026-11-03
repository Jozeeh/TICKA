package sv.edu.itca.ticka.controlador.cliente;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.VentaDAO;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.VentaDAOImpl;
import sv.edu.itca.ticka.modelo.AsientoSeleccionado;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.CompraCliente;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.servicio.CompraServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.servicio.ValidadorPago;
import sv.edu.itca.ticka.util.Formatos;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Pago simulado de la reserva (RF-17, RF-21), al estilo de una pasarela:
 *   GET  ?evento=ID  formulario de pago: datos de facturación y tarjeta, con el
 *                    resumen del pedido y el tiempo que queda de la reserva.
 *   POST             valida que los datos estén escritos (validación ficticia,
 *                    ValidadorPago) y compra la reserva con forma de pago Tarjeta.
 *   GET  ?venta=ID   confirmación del pago aprobado (solo compras propias).
 *
 * Los datos de la tarjeta NO se guardan ni se escriben en el log: solo se
 * revisa que vengan escritos y se descartan.
 */
@WebServlet("/cliente/pago")
public class PagoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/pago.jsp";
    private static final String VISTA_CONFIRMACION = "/WEB-INF/vistas/cliente/pago-confirmado.jsp";
    private static final String[] PAISES = {"El Salvador", "Guatemala", "Honduras", "Nicaragua", "Costa Rica",
        "Panamá", "México", "Estados Unidos", "Otro"};

    private EventoDAO eventoDAO;
    private ClienteDAO clienteDAO;
    private VentaDAO ventaDAO;
    private CompraServicio compraServicio;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        clienteDAO = new ClienteDAOImpl();
        ventaDAO = new VentaDAOImpl();
        compraServicio = new CompraServicio(eventoDAO, ventaDAO);
    }

    private Cliente cliente(HttpServletRequest req) {
        return clienteDAO.buscarPorUsuario(Sesion.usuario(req).getId())
                .orElseThrow(() -> new ServicioException("Su cuenta no tiene datos de cliente."));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Cliente cliente = cliente(req);
            Long idVenta = Parametros.enteroLargo(req.getParameter("venta"));
            if (idVenta != null) {
                mostrarConfirmacion(req, resp, cliente, idVenta);
                return;
            }
            Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
            if (!prepararFormulario(req, cliente, idEvento)) {
                Sesion.error(req, "No tiene asientos reservados para pagar. Elíjalos de nuevo en el mapa.");
                resp.sendRedirect(req.getContextPath() + (idEvento == null ? "/#cartelera" : "/cliente/comprar?evento=" + idEvento));
                return;
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/#cartelera");
            return;
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar el pago. Intente de nuevo.");
            resp.sendRedirect(req.getContextPath() + "/#cartelera");
            return;
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    /** Carga el evento, la reserva vigente y el resumen. Devuelve false si no hay reserva. */
    private boolean prepararFormulario(HttpServletRequest req, Cliente cliente, Long idEvento) {
        Evento evento = idEvento == null ? null : eventoDAO.buscarPorId(idEvento).orElse(null);
        if (evento == null) {
            return false;
        }
        List<AsientoSeleccionado> reserva = ventaDAO.reservados(evento.getId(), cliente.getId());
        if (reserva.isEmpty()) {
            return false;
        }
        BigDecimal total = reserva.stream().map(AsientoSeleccionado::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        req.setAttribute("evento", evento);
        req.setAttribute("cliente", cliente);
        req.setAttribute("reserva", reserva);
        req.setAttribute("total", Formatos.dinero(total));
        req.setAttribute("segundosRestantes",
                Math.max(0, Duration.between(LocalDateTime.now(), reserva.get(0).getReservadoHasta()).getSeconds()));
        req.setAttribute("paises", PAISES);
        return true;
    }

    private void mostrarConfirmacion(HttpServletRequest req, HttpServletResponse resp, Cliente cliente, long idVenta)
            throws ServletException, IOException {
        Optional<CompraCliente> compra = clienteDAO.compras(cliente.getId()).stream()
                .filter(c -> c.getId() == idVenta).findFirst();
        if (compra.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("compra", compra.get());
        req.setAttribute("cliente", cliente);
        req.getRequestDispatcher(VISTA_CONFIRMACION).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        if (idEvento == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            Cliente cliente = cliente(req);
            String error = ValidadorPago.validar(req::getParameter, "si".equals(req.getParameter("terminos")));
            if (error != null) {
                // Se vuelve al formulario con los datos de facturación (los de la tarjeta no se devuelven).
                if (!prepararFormulario(req, cliente, idEvento)) {
                    throw new ServicioException("Su reserva venció y los asientos se liberaron. Elíjalos de nuevo.");
                }
                req.setAttribute("error", error);
                req.getRequestDispatcher(VISTA).forward(req, resp);
                return;
            }
            // Solo tarjeta: es la única forma de pago del portal del cliente.
            long idVenta = compraServicio.comprar(idEvento, cliente.getId(), "Tarjeta");
            resp.sendRedirect(req.getContextPath() + "/cliente/pago?venta=" + idVenta);
            return;
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar el pago. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/cliente/comprar?evento=" + idEvento);
    }
}
