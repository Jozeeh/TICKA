package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.CategoriaDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.ReporteDAO;
import sv.edu.itca.ticka.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.ticka.dao.impl.ReporteDAOImpl;
import sv.edu.itca.ticka.modelo.Categoria;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroReporte;
import sv.edu.itca.ticka.modelo.Reporte;
import sv.edu.itca.ticka.modelo.TipoEvento;
import sv.edu.itca.ticka.util.ExportadorCsv;
import sv.edu.itca.ticka.util.ExportadorPdf;
import sv.edu.itca.ticka.util.Formatos;
import sv.edu.itca.ticka.util.Parametros;

/**
 * Reportes (RF-43 a RF-49) para el Administrador y el Organizador: general
 * por evento, ventas, asistencia, ocupación e incidencias, con filtros por
 * fechas, evento, tipo, categoría y zona. Con formato=pdf el reporte se abre
 * en el navegador (pestaña nueva) y con formato=csv se descarga.
 */
@WebServlet("/reportes")
public class ReporteServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/reportes/reportes.jsp";
    private static final Set<String> TIPOS = Set.of("general", "ventas", "asistencia", "ocupacion", "incidencias");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReporteDAO reporteDAO;
    private CategoriaDAO categoriaDAO;

    @Override
    public void init() {
        reporteDAO = new ReporteDAOImpl();
        categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String tipo = req.getParameter("tipo");
        if (tipo == null || !TIPOS.contains(tipo)) {
            tipo = "general";
        }
        FiltroReporte f = leerFiltro(req);
        if (f.getDesde() != null && f.getHasta() != null && f.getHasta().isBefore(f.getDesde())) {
            req.setAttribute("error", "La fecha final no puede ser anterior a la inicial.");
            f.setHasta(null);
        }

        try {
            Reporte reporte = generar(tipo, f);
            String formato = req.getParameter("formato");
            if ("csv".equals(formato) || "pdf".equals(formato)) {
                descargar(resp, tipo, formato, reporte, describir(tipo, f));
                return;
            }
            req.setAttribute("reporte", reporte);
            req.setAttribute("eventos", reporteDAO.eventosConMapa());
            req.setAttribute("categorias", categoriaDAO.listar());
            req.setAttribute("zonas", reporteDAO.zonasParaFiltro());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo generar el reporte. Verifique la conexión con MySQL.");
            req.setAttribute("eventos", Collections.emptyList());
        }
        req.setAttribute("tipo", tipo);
        req.setAttribute("filtro", f);
        req.setAttribute("tiposEvento", TipoEvento.values());
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    private static FiltroReporte leerFiltro(HttpServletRequest req) {
        FiltroReporte f = new FiltroReporte();
        f.setDesde(Parametros.fecha(req.getParameter("desde")));
        f.setHasta(Parametros.fecha(req.getParameter("hasta")));
        f.setIdEvento(Parametros.enteroLargo(req.getParameter("evento")));
        f.setTipoEvento(TipoEvento.desdeBd(req.getParameter("tipoEvento")));
        f.setIdCategoria(Parametros.enteroLargo(req.getParameter("categoria")));
        f.setIdZona(Parametros.enteroLargo(req.getParameter("zona")));
        return f;
    }

    private Reporte generar(String tipo, FiltroReporte f) {
        switch (tipo) {
            case "general":
                return reporteDAO.general(f);
            case "asistencia":
                return reporteDAO.asistencia(f);
            case "ocupacion":
                return reporteDAO.ocupacion(f);
            case "incidencias":
                return reporteDAO.incidencias(f);
            default:
                return reporteDAO.ventas(f);
        }
    }

    /** Texto con los filtros aplicados, para el encabezado del PDF. */
    private String describir(String tipo, FiltroReporte f) {
        List<String> partes = new ArrayList<>();
        if (f.getDesde() != null) {
            partes.add("desde " + f.getDesde().format(FECHA));
        }
        if (f.getHasta() != null) {
            partes.add("hasta " + f.getHasta().format(FECHA));
        }
        if (f.getIdEvento() != null) {
            reporteDAO.eventosConMapa().stream()
                    .filter(e -> e.getId().equals(f.getIdEvento()))
                    .map(Evento::getNombre).findFirst()
                    .ifPresent(n -> partes.add("evento " + n));
        }
        if (f.getTipoEvento() != null) {
            partes.add("tipo " + f.getTipoEvento().getEtiqueta());
        }
        if (f.getIdCategoria() != null) {
            categoriaDAO.listar().stream()
                    .filter(c -> c.getId().equals(f.getIdCategoria()))
                    .map(Categoria::getNombre).findFirst()
                    .ifPresent(n -> partes.add("categoría " + n));
        }
        if (f.getIdZona() != null && !"general".equals(tipo) && !"incidencias".equals(tipo)) {
            Map<Long, String> zonas = reporteDAO.zonasParaFiltro();
            if (zonas.containsKey(f.getIdZona())) {
                partes.add("zona " + zonas.get(f.getIdZona()));
            }
        }
        return partes.isEmpty() ? "ninguno" : String.join(", ", partes);
    }

    private void descargar(HttpServletResponse resp, String tipo, String formato, Reporte reporte, String filtros)
            throws IOException {
        String archivo = "ticka_" + tipo + "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "." + formato;
        byte[] contenido;
        if ("pdf".equals(formato)) {
            resp.setContentType("application/pdf");
            contenido = ExportadorPdf.generar(reporte, filtros, Formatos.fechaHora(LocalDateTime.now()));
        } else {
            resp.setContentType("text/csv; charset=UTF-8");
            contenido = ExportadorCsv.generar(reporte).getBytes(StandardCharsets.UTF_8);
        }
        // El PDF se muestra en el navegador (inline); el CSV se descarga (attachment).
        String disposicion = "pdf".equals(formato) ? "inline" : "attachment";
        resp.setHeader("Content-Disposition", disposicion + "; filename=\"" + archivo + "\"");
        resp.setContentLength(contenido.length);
        resp.getOutputStream().write(contenido);
    }
}
