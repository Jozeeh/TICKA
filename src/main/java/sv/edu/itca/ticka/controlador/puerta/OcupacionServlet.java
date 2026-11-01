package sv.edu.itca.ticka.controlador.puerta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.AccesoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.impl.AccesoDAOImpl;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.OcupacionPuerta;
import sv.edu.itca.ticka.util.Formatos;
import sv.edu.itca.ticka.util.Json;

/**
 * Ocupación en tiempo real (RF-28): ingresados frente a vendidos por zona.
 * Con formato=json devuelve los datos para que la página se actualice sola
 * cada pocos segundos con fetch (AJAX), sin recargar.
 */
@WebServlet("/puerta/ocupacion")
public class OcupacionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/puerta/ocupacion.jsp";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private AccesoDAO accesoDAO;

    @Override
    public void init() {
        accesoDAO = new AccesoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idEvento = ValidarServlet.eventoDeTrabajo(req);
        if ("json".equals(req.getParameter("formato"))) {
            enviarJson(resp, idEvento);
            return;
        }
        try {
            List<Evento> eventos = accesoDAO.eventosParaPuerta();
            req.setAttribute("eventos", eventos);
            Evento evento = idEvento == null ? null
                    : eventos.stream().filter(e -> e.getId().equals(idEvento)).findFirst().orElse(null);
            req.setAttribute("evento", evento);
            if (evento != null) {
                req.setAttribute("zonas", accesoDAO.ocupacion(evento.getId()));
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo calcular la ocupación.");
            req.setAttribute("eventos", Collections.emptyList());
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    /** {"hora":"..","vendidos":n,"ingresados":n,"porcentaje":n,"zonas":[{"zona":..,"vendidos":..,"ingresados":..,"porcentaje":..}]} */
    private void enviarJson(HttpServletResponse resp, Long idEvento) throws IOException {
        StringBuilder json = new StringBuilder();
        int vendidos = 0;
        int ingresados = 0;
        try {
            List<OcupacionPuerta> zonas = idEvento == null ? Collections.emptyList() : accesoDAO.ocupacion(idEvento);
            json.append("\"zonas\":[");
            for (int i = 0; i < zonas.size(); i++) {
                OcupacionPuerta z = zonas.get(i);
                vendidos += z.getVendidos();
                ingresados += z.getIngresados();
                json.append(i > 0 ? "," : "").append("{\"zona\":").append(Json.texto(z.getZona()))
                        .append(",\"vendidos\":").append(z.getVendidos())
                        .append(",\"ingresados\":").append(z.getIngresados())
                        .append(",\"porcentaje\":").append(z.getPorcentaje()).append('}');
            }
            json.append(']');
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        String cuerpo = "{\"hora\":\"" + LocalTime.now().format(HORA) + "\",\"vendidos\":" + vendidos
                + ",\"ingresados\":" + ingresados + ",\"porcentaje\":" + Formatos.porcentaje(ingresados, vendidos)
                + "," + json + "}";
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }
}
