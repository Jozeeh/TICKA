package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Cierra la sesión. Solo acepta POST (con token CSRF) para que un enlace
 * externo no pueda cerrar la sesión del usuario.
 */
@WebServlet("/cerrar-sesion")
public class CerrarSesionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/acceso?estado=cerrada");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/panel");
    }
}
