package sv.edu.itca.ticka.controlador.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.ReporteDAO;
import sv.edu.itca.ticka.dao.impl.ReporteDAOImpl;
import sv.edu.itca.ticka.modelo.Reporte;
import sv.edu.itca.ticka.util.ExportadorCsv;
import sv.edu.itca.ticka.util.Parametros;

/**
 * Reportes del Administrador (RF-43 a RF-48): ventas, asistencia, ocupación
 * e incidencias, con filtros por rango de fechas y evento. Con
 * formato=csv el mismo reporte se descarga para abrirlo en Excel.
 */
@WebServlet("/admin/reportes")
public class ReporteServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/admin/reportes.jsp";
    private static final Set<String> TIPOS = Set.of("ventas", "asistencia", "ocupacion", "incidencias");

    private ReporteDAO reporteDAO;

    @Override
    public void init() {
        reporteDAO = new ReporteDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String tipo = req.getParameter("tipo");
        if (tipo == null || !TIPOS.contains(tipo)) {
            tipo = "ventas";
        }
        LocalDate desde = Parametros.fecha(req.getParameter("desde"));
        LocalDate hasta = Parametros.fecha(req.getParameter("hasta"));
        Long idEvento = Parametros.enteroLargo(req.getParameter("evento"));
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            req.setAttribute("error", "La fecha final no puede ser anterior a la inicial.");
            hasta = null;
        }

        try {
            Reporte reporte = generar(tipo, desde, hasta, idEvento);
            if ("csv".equals(req.getParameter("formato"))) {
                descargar(resp, tipo, reporte);
                return;
            }
            req.setAttribute("reporte", reporte);
            req.setAttribute("eventos", reporteDAO.eventosConMapa());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo generar el reporte. Verifique la conexión con MySQL.");
            req.setAttribute("eventos", Collections.emptyList());
        }
        req.setAttribute("tipo", tipo);
        req.setAttribute("desde", desde);
        req.setAttribute("hasta", hasta);
        req.setAttribute("idEvento", idEvento);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    private Reporte generar(String tipo, LocalDate desde, LocalDate hasta, Long idEvento) {
        switch (tipo) {
            case "asistencia":
                return reporteDAO.asistencia(desde, hasta, idEvento);
            case "ocupacion":
                return reporteDAO.ocupacion(desde, hasta, idEvento);
            case "incidencias":
                return reporteDAO.incidencias(desde, hasta, idEvento);
            default:
                return reporteDAO.ventas(desde, hasta, idEvento);
        }
    }

    private void descargar(HttpServletResponse resp, String tipo, Reporte reporte) throws IOException {
        String archivo = "ticka_" + tipo + "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + archivo + "\"");
        byte[] contenido = ExportadorCsv.generar(reporte).getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(contenido.length);
        resp.getOutputStream().write(contenido);
    }
}
