package sv.edu.itca.ticka.controlador.cliente;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.AsientoEventoDAO;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.VentaDAO;
import sv.edu.itca.ticka.dao.impl.AsientoEventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.VentaDAOImpl;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.AsientoSeleccionado;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.servicio.CompraServicio;
import sv.edu.itca.ticka.servicio.MapaAsientos;
import sv.edu.itca.ticka.servicio.ReglasCompra;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Configuracion;
import sv.edu.itca.ticka.util.Formatos;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Compra de boletos del cliente (CU-06):
 *   GET  muestra el mapa de asientos (RF-16) y, si el cliente tiene una
 *        reserva vigente, el resumen con el tiempo restante y la forma de pago.
 *   POST accion=reservar  reserva los asientos marcados por N minutos (RF-18) y
 *                         lleva al pago con tarjeta (PagoServlet).
 *   POST accion=cancelar  libera la reserva.
 */
@WebServlet("/cliente/comprar")
public class CompraServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/comprar.jsp";

    private EventoDAO eventoDAO;
    private AsientoEventoDAO asientoEventoDAO;
    private ClienteDAO clienteDAO;
    private VentaDAO ventaDAO;
    private CompraServicio compraServicio;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        asientoEventoDAO = new AsientoEventoDAOImpl();
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
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        try {
            Evento evento = idEvento == null ? null : eventoDAO.buscarPorId(idEvento).orElse(null);
            if (evento == null) {
                Sesion.error(req, "El evento no existe.");
                resp.sendRedirect(req.getContextPath() + "/#cartelera");
                return;
            }
            Cliente cliente = cliente(req);
            String motivo = ReglasCompra.validarVentaAbierta(evento.getEstado(), evento.getFechaInicio(), LocalDateTime.now());
            List<Asiento> asientos = asientoEventoDAO.listarMapaCliente(evento.getId(), cliente.getId());
            List<AsientoSeleccionado> reserva = ventaDAO.reservados(evento.getId(), cliente.getId());

            req.setAttribute("evento", evento);
            req.setAttribute("zonas", MapaAsientos.armarZonas(
                    asientoEventoDAO.listarPrecios(evento.getId(), evento.getIdRecinto()), asientos));
            req.setAttribute("conteo", MapaAsientos.contarPorEstado(asientos));
            req.setAttribute("aLaVenta", motivo == null);
            req.setAttribute("motivoSinVenta", motivo);
            req.setAttribute("maximo", Configuracion.maximoPorCompra());
            req.setAttribute("minutos", Configuracion.minutosReserva());
            req.setAttribute("reserva", reserva);
            if (!reserva.isEmpty()) {
                BigDecimal total = reserva.stream().map(AsientoSeleccionado::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
                LocalDateTime vence = reserva.get(0).getReservadoHasta();
                req.setAttribute("totalReserva", Formatos.dinero(total));
                req.setAttribute("segundosRestantes", Math.max(0, Duration.between(LocalDateTime.now(), vence).getSeconds()));
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
            resp.sendRedirect(req.getContextPath() + "/#cartelera");
            return;
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar el mapa de asientos.");
            resp.sendRedirect(req.getContextPath() + "/#cartelera");
            return;
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        if (idEvento == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String accion = String.valueOf(req.getParameter("accion"));
        String volver = req.getContextPath() + "/cliente/comprar?evento=" + idEvento;
        try {
            Cliente cliente = cliente(req);
            switch (accion) {
                case "reservar":
                    compraServicio.reservar(idEvento, cliente.getId(), Parametros.ids(req.getParameterValues("asiento")));
                    resp.sendRedirect(req.getContextPath() + "/cliente/pago?evento=" + idEvento);
                    return;
                case "cancelar":
                    compraServicio.cancelarReserva(idEvento, cliente.getId());
                    Sesion.exito(req, "Reserva cancelada: los asientos se liberaron.");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar la operación. Intente de nuevo.");
        }
        resp.sendRedirect(volver);
    }
}
