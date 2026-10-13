package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.dao.ZonaDAO;
import sv.edu.itca.ticka.dao.impl.RecintoDAOImpl;
import sv.edu.itca.ticka.dao.impl.ZonaDAOImpl;
import sv.edu.itca.ticka.modelo.Asiento;
import sv.edu.itca.ticka.modelo.FilaAsientos;
import sv.edu.itca.ticka.modelo.Recinto;
import sv.edu.itca.ticka.modelo.Zona;
import sv.edu.itca.ticka.servicio.GeneradorAsientos;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Zonas y asientos de un recinto (RF-11, RF-12, RN-14):
 * crear/editar/eliminar zonas, generar asientos por filas y habilitar o
 * inhabilitar asientos sueltos haciendo clic en la cuadrícula.
 */
@WebServlet("/organizador/zonas")
public class ZonaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/organizador/zonas/lista.jsp";

    private RecintoDAO recintoDAO;
    private ZonaDAO zonaDAO;

    @Override
    public void init() {
        recintoDAO = new RecintoDAOImpl();
        zonaDAO = new ZonaDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idRecinto = Parametros.enteroLargo(req.getParameter("recinto"));
        try {
            Recinto recinto = idRecinto == null ? null : recintoDAO.buscarPorId(idRecinto).orElse(null);
            if (recinto == null) {
                Sesion.error(req, "El recinto no existe.");
                resp.sendRedirect(req.getContextPath() + "/organizador/recintos");
                return;
            }
            Zona edicion = new Zona();
            Long idZona = Parametros.enteroLargo(req.getParameter("zona"));
            if ("editar".equals(req.getParameter("accion")) && idZona != null) {
                edicion = zonaDAO.buscarPorId(idZona).orElse(new Zona());
            }
            mostrar(req, resp, recinto, edicion);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar las zonas. Verifique la conexión con MySQL.");
            resp.sendRedirect(req.getContextPath() + "/organizador/recintos");
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Recinto recinto, Zona edicion)
            throws ServletException, IOException {
        List<Zona> zonas = zonaDAO.listarPorRecinto(recinto.getId());
        agruparAsientos(zonas, zonaDAO.listarAsientosPorRecinto(recinto.getId()));
        req.setAttribute("recinto", recinto);
        req.setAttribute("zonas", zonas);
        req.setAttribute("zona", edicion);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    /** Reparte los asientos (ya ordenados) en sus zonas y filas para la cuadrícula. */
    private void agruparAsientos(List<Zona> zonas, List<Asiento> asientos) {
        Map<Long, Zona> porId = new LinkedHashMap<>();
        for (Zona z : zonas) {
            porId.put(z.getId(), z);
        }
        for (Asiento a : asientos) {
            Zona z = porId.get(a.getIdZona());
            List<FilaAsientos> filas = z.getFilas();
            if (filas.isEmpty() || !filas.get(filas.size() - 1).getLetra().equals(a.getFila())) {
                filas.add(new FilaAsientos(a.getFila()));
            }
            filas.get(filas.size() - 1).getAsientos().add(a);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long idRecinto = Parametros.enteroLargo(req.getParameter("recinto"));
        if (idRecinto == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "guardar":
                    if (!guardar(req, resp, idRecinto)) {
                        return;
                    }
                    break;
                case "eliminar":
                    eliminar(req);
                    break;
                case "generar":
                    generar(req);
                    break;
                case "asiento":
                    cambiarAsiento(req);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar la operación. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/organizador/zonas?recinto=" + idRecinto);
    }

    /** Devuelve false si mostró el formulario con errores (ya respondió). */
    private boolean guardar(HttpServletRequest req, HttpServletResponse resp, long idRecinto)
            throws ServletException, IOException {
        Zona z = new Zona();
        z.setId(Parametros.enteroLargo(req.getParameter("id")));
        z.setIdRecinto(idRecinto);
        z.setNombre(Parametros.texto(req.getParameter("nombre")));
        z.setDescripcion(Parametros.texto(req.getParameter("descripcion")));

        String error = null;
        if (z.getNombre() == null) {
            error = "El nombre de la zona es obligatorio.";
        } else if (z.getNombre().length() > 60) {
            error = "El nombre no puede superar los 60 caracteres.";
        } else if (z.getDescripcion() != null && z.getDescripcion().length() > 200) {
            error = "La descripción no puede superar los 200 caracteres.";
        } else if (zonaDAO.existeNombre(idRecinto, z.getNombre(), z.getId())) {
            error = "El recinto ya tiene una zona con ese nombre.";
        }
        if (error != null) {
            req.setAttribute("error", error);
            mostrar(req, resp, recintoDAO.buscarPorId(idRecinto).orElseThrow(), z);
            return false;
        }
        if (z.getId() == null) {
            zonaDAO.insertar(z);
            Sesion.exito(req, "Zona registrada. Genere sus asientos con el formulario de la zona.");
        } else {
            zonaDAO.actualizar(z);
            Sesion.exito(req, "Zona actualizada.");
        }
        return true;
    }

    private void eliminar(HttpServletRequest req) {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        if (id != null && zonaDAO.eliminar(id)) {
            Sesion.exito(req, "Zona eliminada.");
        } else {
            Sesion.error(req, "No se puede eliminar una zona que ya tiene asientos o precios asignados.");
        }
    }

    private void generar(HttpServletRequest req) {
        Long idZona = Parametros.enteroLargo(req.getParameter("id"));
        if (idZona == null) {
            throw new ServicioException("Zona no válida.");
        }
        List<String> filas = GeneradorAsientos.calcularFilas(req.getParameter("desde"),
                req.getParameter("hasta"), Parametros.entero(req.getParameter("porFila")));
        int porFila = Parametros.entero(req.getParameter("porFila"));
        int creados = zonaDAO.generarAsientos(idZona, filas, porFila);
        int pedidos = filas.size() * porFila;
        Sesion.exito(req, "Se generaron " + creados + " asientos"
                + (creados < pedidos ? " (" + (pedidos - creados) + " ya existían y se omitieron)." : "."));
    }

    /**
     * La cuadrícula es un solo formulario por zona: cada asiento es un botón
     * con name="cambio" y value="idAsiento:si" (habilitar) o "idAsiento:no".
     */
    private void cambiarAsiento(HttpServletRequest req) {
        String[] cambio = String.valueOf(req.getParameter("cambio")).split(":");
        Long idAsiento = cambio.length == 2 ? Parametros.enteroLargo(cambio[0]) : null;
        boolean activar = cambio.length == 2 && "si".equals(cambio[1]);
        if (idAsiento == null || !zonaDAO.cambiarActivoAsiento(idAsiento, activar)) {
            throw new ServicioException("No se pudo actualizar el asiento.");
        }
        Sesion.exito(req, activar ? "Asiento habilitado." : "Asiento inhabilitado: no se incluirá en los próximos eventos.");
    }
}
