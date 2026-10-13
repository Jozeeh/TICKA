package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Panel principal después del inicio de sesión. Muestra los módulos que le
 * corresponden al rol del usuario (la vista decide con el rol en sesión).
 * Protegido por AutenticacionFilter.
 */
@WebServlet("/panel")
public class PanelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/vistas/panel/panel.jsp").forward(req, resp);
    }
}
