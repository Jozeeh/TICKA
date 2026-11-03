package sv.edu.itca.ticka.controlador.cliente;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.controlador.InicioServlet;
import sv.edu.itca.ticka.dao.CategoriaDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.RecintoDAOImpl;
import sv.edu.itca.ticka.modelo.FiltroEvento;
import sv.edu.itca.ticka.modelo.TipoEvento;

/**
 * Cartelera dentro del panel del cliente (RF-15): los mismos eventos, filtros y
 * tarjetas que la página de inicio, pero con el menú lateral, y cada tarjeta
 * lleva directo a elegir asientos.
 */
@WebServlet("/cliente/cartelera")
public class CarteleraServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/cartelera.jsp";

    private EventoDAO eventoDAO;
    private CategoriaDAO categoriaDAO;
    private RecintoDAO recintoDAO;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        categoriaDAO = new CategoriaDAOImpl();
        recintoDAO = new RecintoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        FiltroEvento filtro = InicioServlet.leerFiltro(req);
        try {
            req.setAttribute("eventos", eventoDAO.listarCartelera(filtro));
            req.setAttribute("categorias", categoriaDAO.listar());
            req.setAttribute("recintos", recintoDAO.listarActivos());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "La cartelera no está disponible por el momento.");
            req.setAttribute("eventos", Collections.emptyList());
        }
        req.setAttribute("filtro", filtro);
        req.setAttribute("tipos", TipoEvento.values());
        req.setAttribute("tipoActivo", filtro.getTipo());
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }
}
