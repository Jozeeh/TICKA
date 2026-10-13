package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.CategoriaDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.ticka.modelo.Categoria;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Catálogo de categorías (RF-04): listado con formulario en la misma página
 * para crear o editar, y eliminación cuando ningún evento la usa.
 */
@WebServlet("/organizador/categorias")
public class CategoriaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/organizador/categorias/lista.jsp";

    private CategoriaDAO categoriaDAO;

    @Override
    public void init() {
        categoriaDAO = new CategoriaDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Categoria edicion = new Categoria();
            Long id = Parametros.enteroLargo(req.getParameter("id"));
            if ("editar".equals(req.getParameter("accion")) && id != null) {
                edicion = categoriaDAO.buscarPorId(id).orElse(new Categoria());
            }
            mostrar(req, resp, edicion);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar las categorías. Verifique la conexión con MySQL.");
            req.setAttribute("categorias", Collections.emptyList());
            req.setAttribute("categoria", new Categoria());
            req.getRequestDispatcher(VISTA).forward(req, resp);
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Categoria edicion)
            throws ServletException, IOException {
        req.setAttribute("categoria", edicion);
        req.setAttribute("categorias", categoriaDAO.listar());
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(req, resp);
                return;
            } else if ("eliminar".equals(accion)) {
                eliminar(req);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar la operación. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/organizador/categorias");
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Categoria c = new Categoria(Parametros.enteroLargo(req.getParameter("id")),
                Parametros.texto(req.getParameter("nombre")));
        String error = null;
        if (c.getNombre() == null) {
            error = "El nombre de la categoría es obligatorio.";
        } else if (c.getNombre().length() > 60) {
            error = "El nombre no puede superar los 60 caracteres.";
        } else if (categoriaDAO.existeNombre(c.getNombre(), c.getId())) {
            error = "Ya existe una categoría con ese nombre.";
        }
        if (error != null) {
            req.setAttribute("error", error);
            mostrar(req, resp, c);
            return;
        }
        if (c.getId() == null) {
            categoriaDAO.insertar(c);
            Sesion.exito(req, "Categoría registrada.");
        } else {
            categoriaDAO.actualizar(c);
            Sesion.exito(req, "Categoría actualizada.");
        }
        resp.sendRedirect(req.getContextPath() + "/organizador/categorias");
    }

    private void eliminar(HttpServletRequest req) {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        if (id != null && categoriaDAO.eliminar(id)) {
            Sesion.exito(req, "Categoría eliminada.");
        } else {
            Sesion.error(req, "No se puede eliminar: la categoría está asignada a uno o más eventos.");
        }
    }
}
