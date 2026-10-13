package sv.edu.itca.ticka.controlador.admin;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.ReporteDAO;
import sv.edu.itca.ticka.dao.impl.ReporteDAOImpl;
import sv.edu.itca.ticka.modelo.Indicadores;

/**
 * Dashboard del Administrador (RF-43): indicadores generales y próximos
 * eventos con su avance de ventas. Solo lectura.
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
            req.setAttribute("proximos", reporteDAO.proximosEventos(6));
            req.setAttribute("ocupacion", reporteDAO.ocupacion(null, null, null));
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo calcular los indicadores. Verifique la conexión con MySQL.");
            req.setAttribute("indicadores", new Indicadores());
            req.setAttribute("proximos", Collections.emptyList());
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }
}
