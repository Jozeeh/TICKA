package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.AsientoEventoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.impl.AsientoEventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.servicio.ReglasCompra;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Detalle público de un evento de la cartelera (RF-15): descripción,
 * participantes, horario, precios y disponibilidad por zona, y el botón
 * para elegir asientos (pide iniciar sesión como cliente).
 */
@WebServlet("/evento")
public class EventoPublicoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/evento.jsp";

    private EventoDAO eventoDAO;
    private AsientoEventoDAO asientoEventoDAO;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        asientoEventoDAO = new AsientoEventoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        try {
            Evento e = id == null ? null : eventoDAO.buscarPorId(id).orElse(null);
            // Solo se muestran eventos visibles en cartelera (no borradores ni cancelados).
            if (e == null || e.getEstado() == EstadoEvento.BORRADOR || e.getEstado() == EstadoEvento.CANCELADO) {
                Sesion.error(req, "El evento no está disponible.");
                resp.sendRedirect(req.getContextPath() + "/#cartelera");
                return;
            }
            req.setAttribute("evento", e);
            req.setAttribute("precios", asientoEventoDAO.listarPrecios(e.getId(), e.getIdRecinto()));
            req.setAttribute("ocupacion", asientoEventoDAO.ocupacionPorEvento(e.getId()));
            String motivo = ReglasCompra.validarVentaAbierta(e.getEstado(), e.getFechaInicio(), LocalDateTime.now());
            req.setAttribute("aLaVenta", motivo == null);
            req.setAttribute("motivoSinVenta", motivo);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar el evento.");
            resp.sendRedirect(req.getContextPath() + "/#cartelera");
            return;
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }
}
