package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
 * Zonas y asientos de un recinto (RF-11, RF-12, RN-14).
 *
 * Cada zona se configura con un solo formulario ("Guardar zona"): nombre,
 * descripción y distribución de asientos (fila desde, fila hasta y asientos
 * por fila). Al guardar se crean los asientos que falten y se inactivan, sin
 * borrarlos, los que quedan fuera de la cuadrícula. Los asientos sueltos se
 * habilitan o inhabilitan con un clic en la cuadrícula.
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
            mostrar(req, resp, recinto);
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo cargar las zonas. Verifique la conexión con MySQL.");
            resp.sendRedirect(req.getContextPath() + "/organizador/recintos");
        }
    }

    private void mostrar(HttpServletRequest req, HttpServletResponse resp, Recinto recinto)
            throws ServletException, IOException {
        req.setAttribute("recinto", recinto);
        req.setAttribute("zonas", cargarZonas(recinto.getId()));
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    /** Zonas del recinto con su cuadrícula de asientos y su distribución actual. */
    private List<Zona> cargarZonas(long idRecinto) {
        List<Zona> zonas = zonaDAO.listarPorRecinto(idRecinto);
        Map<Long, Zona> porId = new LinkedHashMap<>();
        for (Zona z : zonas) {
            porId.put(z.getId(), z);
        }
        // Los asientos llegan ordenados por zona, fila y número.
        for (Asiento a : zonaDAO.listarAsientosPorRecinto(idRecinto)) {
            Zona z = porId.get(a.getIdZona());
            List<FilaAsientos> filas = z.getFilas();
            if (filas.isEmpty() || !filas.get(filas.size() - 1).getLetra().equals(a.getFila())) {
                filas.add(new FilaAsientos(a.getFila()));
            }
            filas.get(filas.size() - 1).getAsientos().add(a);
            if (a.isActivo()) {
                if (z.getFilaDesde() == null || a.getFila().compareTo(z.getFilaDesde()) < 0) {
                    z.setFilaDesde(a.getFila());
                }
                if (z.getFilaHasta() == null || a.getFila().compareTo(z.getFilaHasta()) > 0) {
                    z.setFilaHasta(a.getFila());
                }
                if (z.getAsientosPorFila() == null || a.getNumero() > z.getAsientosPorFila()) {
                    z.setAsientosPorFila(a.getNumero());
                }
            }
        }
        return zonas;
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
                    guardar(req, idRecinto);
                    break;
                case "eliminar":
                    eliminar(req);
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

    /** Crea o actualiza la zona y aplica su distribución de asientos. */
    private void guardar(HttpServletRequest req, long idRecinto) {
        Zona z = new Zona();
        z.setId(Parametros.enteroLargo(req.getParameter("id")));
        z.setIdRecinto(idRecinto);
        z.setNombre(Parametros.texto(req.getParameter("nombre")));
        z.setDescripcion(Parametros.texto(req.getParameter("descripcion")));

        if (z.getNombre() == null) {
            throw new ServicioException("El nombre de la zona es obligatorio.");
        }
        if (z.getNombre().length() > 60) {
            throw new ServicioException("El nombre no puede superar los 60 caracteres.");
        }
        if (z.getDescripcion() != null && z.getDescripcion().length() > 200) {
            throw new ServicioException("La descripción no puede superar los 200 caracteres.");
        }
        if (zonaDAO.existeNombre(idRecinto, z.getNombre(), z.getId())) {
            throw new ServicioException("El recinto ya tiene una zona con ese nombre.");
        }
        // Se valida la distribución antes de guardar nada.
        String desde = req.getParameter("desde");
        String hasta = req.getParameter("hasta");
        Integer porFila = Parametros.entero(req.getParameter("porFila"));
        List<String> filas = GeneradorAsientos.calcularFilas(desde, hasta, porFila);

        boolean esNueva = z.getId() == null;
        boolean cambiaDistribucion = true;
        if (esNueva) {
            z.setId(zonaDAO.insertar(z));
        } else {
            Zona actual = cargarZonas(idRecinto).stream()
                    .filter(x -> x.getId().equals(z.getId())).findFirst()
                    .orElseThrow(() -> new ServicioException("La zona no existe."));
            zonaDAO.actualizar(z);
            // Si solo cambió el nombre o la descripción no se tocan los asientos, así se
            // conservan los que se inhabilitaron uno por uno.
            cambiaDistribucion = !(filas.get(0).equals(actual.getFilaDesde())
                    && filas.get(filas.size() - 1).equals(actual.getFilaHasta())
                    && Objects.equals(porFila, actual.getAsientosPorFila()));
        }

        String mensaje = esNueva ? "Zona registrada" : "Zona actualizada";
        if (cambiaDistribucion) {
            int[] r = zonaDAO.configurarAsientos(z.getId(), filas, porFila);
            mensaje += ": " + filas.size() + " filas × " + porFila + " asientos ("
                    + r[0] + " creados, " + r[1] + " reactivados, " + r[2] + " inactivados).";
        } else {
            mensaje += ".";
        }
        Sesion.exito(req, mensaje);
    }

    private void eliminar(HttpServletRequest req) {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        if (id != null && zonaDAO.eliminar(id)) {
            Sesion.exito(req, "Zona eliminada.");
        } else {
            Sesion.error(req, "No se puede eliminar una zona que ya tiene asientos o precios asignados.");
        }
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
