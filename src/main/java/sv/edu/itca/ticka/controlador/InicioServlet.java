package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.TipoEvento;

/**
 * Página de inicio pública con la cartelera (RF-15): eventos publicados en
 * tarjetas con fecha, recinto, precios por zona y disponibilidad, filtrables
 * por tipo. El mapeo "" atiende exactamente la raíz de la aplicación (/ticka/).
 */
@WebServlet(urlPatterns = {"", "/inicio"})
public class InicioServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/inicio.jsp";

    private EventoDAO eventoDAO;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TipoEvento tipo = TipoEvento.desdeBd(req.getParameter("tipo"));
        try {
            // Los indicadores de la portada se calculan con toda la cartelera.
            List<Evento> todos = eventoDAO.listarCartelera(null);
            List<Evento> eventos = tipo == null ? todos : eventoDAO.listarCartelera(tipo);
            req.setAttribute("eventos", eventos);
            req.setAttribute("totalEventos", todos.size());
            req.setAttribute("totalDisponibles", todos.stream().mapToInt(Evento::getDisponibles).sum());
            Set<String> recintos = new HashSet<>();
            todos.forEach(e -> recintos.add(e.getRecinto()));
            req.setAttribute("totalRecintos", recintos.size());
        } catch (DAOException | IllegalStateException ex) {
            log("No se pudo cargar la cartelera", ex);
            req.setAttribute("error", "La cartelera no está disponible por el momento (sin conexión con la base de datos).");
            req.setAttribute("eventos", Collections.emptyList());
        }
        req.setAttribute("tipos", TipoEvento.values());
        req.setAttribute("tipoActivo", tipo);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }
}
