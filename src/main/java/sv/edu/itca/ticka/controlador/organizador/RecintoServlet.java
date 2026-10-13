package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.dao.impl.RecintoDAOImpl;
import sv.edu.itca.ticka.modelo.Recinto;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Mantenimiento de recintos (RF-10). Los recintos no se eliminan: se
 * desactivan para conservar el historial de eventos (borrado lógico).
 * Las zonas y asientos de cada recinto se gestionan en ZonaServlet.
 */
@WebServlet("/organizador/recintos")
public class RecintoServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/vistas/organizador/recintos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/organizador/recintos/formulario.jsp";

    private RecintoDAO recintoDAO;

    @Override
    public void init() {
        recintoDAO = new RecintoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                mostrarFormulario(req, resp, new Recinto());
            } else if ("editar".equals(accion)) {
                Long id = Parametros.enteroLargo(req.getParameter("id"));
                Recinto r = id == null ? null : recintoDAO.buscarPorId(id).orElse(null);
                if (r == null) {
                    Sesion.error(req, "El recinto no existe.");
                    resp.sendRedirect(req.getContextPath() + "/organizador/recintos");
                    return;
                }
                mostrarFormulario(req, resp, r);
            } else {
                req.setAttribute("recintos", recintoDAO.listar());
                req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar los recintos. Verifique la conexión con MySQL.");
            req.setAttribute("recintos", Collections.emptyList());
            req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Recinto r)
            throws ServletException, IOException {
        req.setAttribute("recinto", r);
        req.setAttribute("tipos", Recinto.TIPOS);
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("guardar".equals(accion)) {
                guardar(req, resp);
                return;
            } else if ("estado".equals(accion)) {
                cambiarEstado(req);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar la operación. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/organizador/recintos");
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Recinto r = new Recinto();
        r.setId(Parametros.enteroLargo(req.getParameter("id")));
        r.setNombre(Parametros.texto(req.getParameter("nombre")));
        r.setDireccion(Parametros.texto(req.getParameter("direccion")));
        r.setTipo(Parametros.texto(req.getParameter("tipo")));
        r.setActivo(r.getId() == null || "si".equals(req.getParameter("activo")));

        String error = null;
        if (r.getNombre() == null) {
            error = "El nombre del recinto es obligatorio.";
        } else if (r.getNombre().length() > 120) {
            error = "El nombre no puede superar los 120 caracteres.";
        } else if (r.getDireccion() != null && r.getDireccion().length() > 200) {
            error = "La dirección no puede superar los 200 caracteres.";
        } else if (r.getTipo() == null || !Arrays.asList(Recinto.TIPOS).contains(r.getTipo())) {
            error = "Seleccione un tipo de recinto válido.";
        } else if (recintoDAO.existeNombre(r.getNombre(), r.getId())) {
            error = "Ya existe un recinto con ese nombre.";
        }
        if (error != null) {
            req.setAttribute("error", error);
            mostrarFormulario(req, resp, r);
            return;
        }

        if (r.getId() == null) {
            long id = recintoDAO.insertar(r);
            Sesion.exito(req, "Recinto registrado. Ahora agregue sus zonas y genere los asientos.");
            resp.sendRedirect(req.getContextPath() + "/organizador/zonas?recinto=" + id);
            return;
        }
        recintoDAO.actualizar(r);
        Sesion.exito(req, "Recinto actualizado.");
        resp.sendRedirect(req.getContextPath() + "/organizador/recintos");
    }

    private void cambiarEstado(HttpServletRequest req) {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        Recinto r = id == null ? null : recintoDAO.buscarPorId(id).orElse(null);
        if (r == null) {
            Sesion.error(req, "El recinto no existe.");
            return;
        }
        r.setActivo(!r.isActivo());
        recintoDAO.actualizar(r);
        Sesion.exito(req, r.isActivo() ? "Recinto activado." : "Recinto desactivado: ya no se ofrecerá para nuevos eventos.");
    }
}
