package sv.edu.itca.ticka.controlador.admin;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.ReporteDAO;
import sv.edu.itca.ticka.dao.impl.ReporteDAOImpl;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroReporte;
import sv.edu.itca.ticka.modelo.Indicadores;
import sv.edu.itca.ticka.util.Json;

/**
 * Dashboard del Administrador (RF-42, RF-53): indicadores generales,
 * gráficos (ingresos por mes, por canal, eventos con más ingresos y
 * ocupación de los próximos eventos) y próximos eventos. Solo lectura.
 * Las series se pasan a la vista en JSON para Chart.js.
 */
@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/admin/dashboard.jsp";

    private ReporteDAO reporteDAO;

    @Override
    public void init() {
        reporteDAO = new ReporteDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("indicadores", reporteDAO.indicadores());
            List<Evento> proximos = reporteDAO.proximosEventos(6);
            req.setAttribute("proximos", proximos);
            Map<String, Integer> ocupacion = new LinkedHashMap<>();
            for (Evento e : proximos) {
                ocupacion.put(e.getNombre(), e.getPorcentajeVendido());
            }
            req.setAttribute("graficoMeses", Json.serie(reporteDAO.ingresosPorMes(6)));
            req.setAttribute("graficoCanal", Json.serie(reporteDAO.ingresosPorCanal()));
            req.setAttribute("graficoTop", Json.serie(reporteDAO.topEventosPorIngresos(5)));
            req.setAttribute("graficoOcupacion", Json.serie(ocupacion));
            req.setAttribute("ocupacion", reporteDAO.ocupacion(new FiltroReporte()));
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo calcular los indicadores. Verifique la conexión con MySQL.");
            req.setAttribute("indicadores", new Indicadores());
            req.setAttribute("proximos", Collections.emptyList());
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }
}
