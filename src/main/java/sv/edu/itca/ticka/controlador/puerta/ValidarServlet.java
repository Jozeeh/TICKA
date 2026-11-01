package sv.edu.itca.ticka.controlador.puerta;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sv.edu.itca.ticka.dao.AccesoDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.impl.AccesoDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.IncidenciaDAOImpl;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.ResultadoValidacion;
import sv.edu.itca.ticka.servicio.AccesoServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Configuracion;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Validación de boletos en puerta (CU-08, RF-25 a RF-31):
 *   GET   pantalla con el evento y la puerta de trabajo, el último resultado
 *         (VÁLIDO / RECHAZADO), la búsqueda por documento y la bitácora.
 *   POST accion=validar  valida el código escrito o escaneado.
 *   POST accion=anular   anula un ingreso registrado por error, con motivo.
 * El evento y la puerta se recuerdan en la sesión para no elegirlos en cada lectura.
 */
@WebServlet("/puerta/validar")
public class ValidarServlet extends HttpServlet {

    static final String EVENTO_SESION = "puertaEvento";
    static final String PUERTA_SESION = "puertaNombre";
    private static final String RESULTADO_SESION = "puertaResultado";
    private static final String VISTA = "/WEB-INF/vistas/puerta/validar.jsp";

    private AccesoDAO accesoDAO;
    private AccesoServicio accesoServicio;

    @Override
    public void init() {
        accesoDAO = new AccesoDAOImpl();
        accesoServicio = new AccesoServicio(accesoDAO, new EventoDAOImpl(), new IncidenciaDAOImpl());
    }

    /** Evento de trabajo: el del parámetro, o el que quedó en la sesión. */
    static Long eventoDeTrabajo(HttpServletRequest req) {
        HttpSession s = req.getSession();
        Long id = Parametros.enteroLargo(req.getParameter("evento"));
        if (id != null) {
            s.setAttribute(EVENTO_SESION, id);
        }
        return (Long) s.getAttribute(EVENTO_SESION);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession();
        Long idEvento = eventoDeTrabajo(req);
        String puerta = Parametros.texto(req.getParameter("puerta"));
        if (puerta != null) {
            s.setAttribute(PUERTA_SESION, puerta);
        }
        try {
            List<Evento> eventos = accesoDAO.eventosParaPuerta();
            req.setAttribute("eventos", eventos);
            Evento evento = idEvento == null ? null
                    : eventos.stream().filter(e -> e.getId().equals(idEvento)).findFirst().orElse(null);
            req.setAttribute("evento", evento);
            if (evento != null) {
                req.setAttribute("accesos", accesoDAO.recientes(evento.getId(), 15));
                String documento = Parametros.texto(req.getParameter("documento"));
                if (documento != null) {
                    req.setAttribute("documento", documento);
                    req.setAttribute("boletosDocumento", accesoDAO.porDocumento(evento.getId(), documento.toUpperCase()));
                }
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar la pantalla de validación.");
            req.setAttribute("eventos", Collections.emptyList());
        }
        // Último resultado (se muestra una sola vez).
        req.setAttribute("resultado", s.getAttribute(RESULTADO_SESION));
        s.removeAttribute(RESULTADO_SESION);
        req.setAttribute("puerta", s.getAttribute(PUERTA_SESION) == null ? "Puerta 1" : s.getAttribute(PUERTA_SESION));
        req.setAttribute("reentrada", Configuracion.reentrada());
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession s = req.getSession();
        Long idEvento = eventoDeTrabajo(req);
        String accion = String.valueOf(req.getParameter("accion"));
        try {
            if (idEvento == null) {
                throw new ServicioException("Seleccione el evento que está controlando.");
            }
            if ("validar".equals(accion)) {
                String puerta = req.getParameter("puerta");
                s.setAttribute(PUERTA_SESION, puerta == null ? null : puerta.trim());
                ResultadoValidacion r = accesoServicio.validar(req.getParameter("codigo"), idEvento, puerta,
                        Sesion.usuario(req).getId());
                s.setAttribute(RESULTADO_SESION, r);
            } else if ("anular".equals(accion)) {
                Long idAcceso = Parametros.enteroLargo(req.getParameter("acceso"));
                if (idAcceso == null) {
                    throw new ServicioException("Ingreso no válido.");
                }
                accesoServicio.anularIngreso(idAcceso, req.getParameter("motivo"));
                Sesion.exito(req, "Ingreso anulado: el boleto puede volver a usarse.");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar la operación. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/puerta/validar");
    }
}
