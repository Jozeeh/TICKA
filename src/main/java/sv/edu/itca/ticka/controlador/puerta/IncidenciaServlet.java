package sv.edu.itca.ticka.controlador.puerta;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.AccesoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.IncidenciaDAO;
import sv.edu.itca.ticka.dao.ReporteDAO;
import sv.edu.itca.ticka.dao.impl.AccesoDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.IncidenciaDAOImpl;
import sv.edu.itca.ticka.dao.impl.ReporteDAOImpl;
import sv.edu.itca.ticka.modelo.Incidencia;
import sv.edu.itca.ticka.servicio.IncidenciaServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Incidencias (M5):
 *   GET                  listado con filtros por evento, tipo, gravedad y estado (RF-34)
 *                        y formulario para registrar una nueva (RF-32).
 *   POST accion=registrar  registra la incidencia.
 *   POST accion=estado     seguimiento: En atención o Cerrada, con observaciones (RF-33).
 */
@WebServlet("/puerta/incidencias")
public class IncidenciaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/puerta/incidencias.jsp";

    private IncidenciaDAO incidenciaDAO;
    private AccesoDAO accesoDAO;
    private IncidenciaServicio incidenciaServicio;
    private ReporteDAO reporteDAO;

    @Override
    public void init() {
        reporteDAO = new ReporteDAOImpl();
        incidenciaDAO = new IncidenciaDAOImpl();
        accesoDAO = new AccesoDAOImpl();
        incidenciaServicio = new IncidenciaServicio(incidenciaDAO, new EventoDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        String tipo = Parametros.texto(req.getParameter("tipo"));
        String gravedad = Parametros.texto(req.getParameter("gravedad"));
        String estado = Parametros.texto(req.getParameter("estado"));
        try {
            req.setAttribute("eventos", accesoDAO.eventosParaPuerta());
            // Para consultar se ofrecen todos los eventos con mapa, también los ya terminados.
            req.setAttribute("eventosFiltro", reporteDAO.eventosConMapa());
            req.setAttribute("incidencias", incidenciaDAO.listar(idEvento, tipo, gravedad, estado, 100));
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar las incidencias.");
            req.setAttribute("eventos", Collections.emptyList());
            req.setAttribute("incidencias", Collections.emptyList());
        }
        req.setAttribute("idEvento", idEvento);
        req.setAttribute("tipoFiltro", tipo);
        req.setAttribute("gravedadFiltro", gravedad);
        req.setAttribute("estadoFiltro", estado);
        // Evento de trabajo de la puerta como valor por defecto del formulario.
        req.setAttribute("eventoTrabajo", req.getSession().getAttribute(ValidarServlet.EVENTO_SESION));
        req.setAttribute("puertaTrabajo", req.getSession().getAttribute(ValidarServlet.PUERTA_SESION));
        req.setAttribute("tipos", Incidencia.TIPOS);
        req.setAttribute("gravedades", Incidencia.GRAVEDADES);
        req.setAttribute("estados", Incidencia.ESTADOS);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accion = String.valueOf(req.getParameter("accion"));
        try {
            if ("registrar".equals(accion)) {
                Incidencia i = new Incidencia();
                i.setIdEvento(Parametros.enteroLargo(req.getParameter("evento")));
                i.setTipo(req.getParameter("tipo"));
                i.setGravedad(req.getParameter("gravedad"));
                i.setPuerta(req.getParameter("puerta"));
                i.setDescripcion(req.getParameter("descripcion"));
                i.setIdUsuario(Sesion.usuario(req).getId());
                incidenciaServicio.registrar(i);
                Sesion.exito(req, "Incidencia registrada.");
            } else if ("estado".equals(accion)) {
                Long id = Parametros.enteroLargo(req.getParameter("id"));
                if (id == null) {
                    throw new ServicioException("Incidencia no válida.");
                }
                incidenciaServicio.cambiarEstado(id, req.getParameter("estado"), req.getParameter("observaciones"));
                Sesion.exito(req, "Seguimiento guardado.");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo guardar la incidencia.");
        }
        resp.sendRedirect(req.getContextPath() + "/puerta/incidencias");
    }
}
