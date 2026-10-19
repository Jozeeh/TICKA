package sv.edu.itca.ticka.controlador;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.modelo.Afiche;
import sv.edu.itca.ticka.util.Parametros;

/**
 * Entrega el afiche de un evento guardado en MySQL (/afiche?evento=ID).
 * Es público porque la cartelera de la página de inicio lo muestra. Si el
 * evento no tiene afiche, redirige a la imagen por defecto.
 */
@WebServlet("/afiche")
public class AficheServlet extends HttpServlet {

    private EventoDAO eventoDAO;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = Parametros.enteroLargo(req.getParameter("evento"));
        Optional<Afiche> afiche = Optional.empty();
        try {
            if (id != null) {
                afiche = eventoDAO.obtenerAfiche(id);
            }
        } catch (DAOException ex) {
            log("No se pudo leer el afiche", ex);
        }
        if (afiche.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/resources/img/sin-imagen.png");
            return;
        }
        byte[] contenido = afiche.get().getContenido();
        resp.setContentType(afiche.get().getTipo());
        resp.setContentLength(contenido.length);
        // no-cache: si el organizador cambia el afiche, se ve el nuevo de inmediato.
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.getOutputStream().write(contenido);
    }
}
