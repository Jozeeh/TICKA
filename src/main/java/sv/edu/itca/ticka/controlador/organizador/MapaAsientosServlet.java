package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import sv.edu.itca.ticka.dao.impl.RecintoDAOImpl;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FilaAsientos;
import sv.edu.itca.ticka.modelo.PrecioZona;
import sv.edu.itca.ticka.modelo.ZonaMapa;
import sv.edu.itca.ticka.servicio.EventoServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Mapa de asientos de un evento (cuadrícula por zona y fila, coloreada por
 * estado). El Organizador puede bloquear o desbloquear asientos disponibles
 * con un clic (RF-08). El portal del cliente reutilizará esta misma
 * estructura para elegir asientos.
 */
@WebServlet("/organizador/mapa")
public class MapaAsientosServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/organizador/eventos/mapa.jsp";

    private EventoDAO eventoDAO;
    private AsientoEventoDAO asientoEventoDAO;
    private EventoServicio eventoServicio;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        asientoEventoDAO = new AsientoEventoDAOImpl();
        eventoServicio = new EventoServicio(eventoDAO, new RecintoDAOImpl(), asientoEventoDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long id = Parametros.enteroLargo(req.getParameter("evento"));
        try {
            Evento evento = id == null ? null : eventoDAO.buscarPorId(id).orElse(null);
            if (evento == null) {
                Sesion.error(req, "El evento no existe.");
                resp.sendRedirect(req.getContextPath() + "/organizador/eventos");
                return;
            }
            List<Asiento> asientos = asientoEventoDAO.listarMapa(id);
            req.setAttribute("evento", evento);
            req.setAttribute("zonas", armarZonas(evento, asientos));
            req.setAttribute("conteo", contarPorEstado(asientos));
            req.setAttribute("puedeBloquear", evento.getEstado().name().equals("PUBLICADO")
                    || evento.getEstado().name().equals("AGOTADO"));
            req.getRequestDispatcher(VISTA).forward(req, resp);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar el mapa de asientos.");
            resp.sendRedirect(req.getContextPath() + "/organizador/eventos");
        }
    }

    /** Agrupa los asientos (ya ordenados) por zona y fila, con el precio de la zona. */
    private List<ZonaMapa> armarZonas(Evento evento, List<Asiento> asientos) {
        Map<Long, ZonaMapa> zonas = new LinkedHashMap<>();
        for (PrecioZona p : asientoEventoDAO.listarPrecios(evento.getId(), evento.getIdRecinto())) {
            ZonaMapa z = new ZonaMapa(p.getIdZona(), p.getZona());
            z.setPrecio(p.getPrecio());
            zonas.put(p.getIdZona(), z);
        }
        for (Asiento a : asientos) {
            ZonaMapa z = zonas.get(a.getIdZona());
            List<FilaAsientos> filas = z.getFilas();
            if (filas.isEmpty() || !filas.get(filas.size() - 1).getLetra().equals(a.getFila())) {
                filas.add(new FilaAsientos(a.getFila()));
            }
            filas.get(filas.size() - 1).getAsientos().add(a);
        }
        // Solo se muestran las zonas que tienen asientos en este evento.
        zonas.values().removeIf(z -> z.getFilas().isEmpty());
        return List.copyOf(zonas.values());
    }

    private Map<String, Integer> contarPorEstado(List<Asiento> asientos) {
        Map<String, Integer> conteo = new TreeMap<>();
        for (String estado : new String[]{"Disponible", "Reservado", "Vendido", "Bloqueado"}) {
            conteo.put(estado, 0);
        }
        for (Asiento a : asientos) {
            conteo.merge(a.getEstado(), 1, Integer::sum);
        }
        return conteo;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Cada asiento del mapa es un botón name="cambio" value="idEventoAsiento:si|no".
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        String[] cambio = String.valueOf(req.getParameter("cambio")).split(":");
        Long idEventoAsiento = cambio.length == 2 ? Parametros.enteroLargo(cambio[0]) : null;
        if (idEvento == null || idEventoAsiento == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        boolean bloquear = "si".equals(cambio[1]);
        try {
            eventoServicio.cambiarBloqueo(idEvento, idEventoAsiento, bloquear);
            Sesion.exito(req, bloquear ? "Asiento bloqueado." : "Asiento desbloqueado.");
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cambiar el asiento.");
        }
        resp.sendRedirect(req.getContextPath() + "/organizador/mapa?evento=" + idEvento);
    }
}
