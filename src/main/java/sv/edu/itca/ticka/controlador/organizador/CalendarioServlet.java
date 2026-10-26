package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.RecintoDAOImpl;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.servicio.GeneradorCalendario;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Calendario mensual de eventos del Organizador (RF-50). Parámetros
 * opcionales: mes (AAAA-MM), recinto y mios=si (solo los propios, RF-52).
 */
@WebServlet("/organizador/calendario")
public class CalendarioServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/organizador/eventos/calendario.jsp";
    private static final Locale ES = new Locale("es", "SV");

    private EventoDAO eventoDAO;
    private RecintoDAO recintoDAO;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        recintoDAO = new RecintoDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        YearMonth mes = leerMes(req.getParameter("mes"));
        Long idRecinto = Parametros.enteroLargo(req.getParameter("recinto"));
        boolean mios = "si".equals(req.getParameter("mios"));
        Long idCreador = mios ? Sesion.usuario(req).getId() : null;

        List<Evento> eventos = Collections.emptyList();
        try {
            eventos = eventoDAO.listarCalendario(GeneradorCalendario.primerDiaVisible(mes),
                    GeneradorCalendario.finVisible(mes), idRecinto, idCreador);
            req.setAttribute("recintos", recintoDAO.listar());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar el calendario. Verifique la conexión con MySQL.");
            req.setAttribute("recintos", Collections.emptyList());
        }
        String nombreMes = mes.getMonth().getDisplayName(TextStyle.FULL, ES);
        req.setAttribute("tituloMes", Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1) + " " + mes.getYear());
        req.setAttribute("mes", mes);
        req.setAttribute("mesAnterior", mes.minusMonths(1));
        req.setAttribute("mesSiguiente", mes.plusMonths(1));
        req.setAttribute("mesActual", YearMonth.now());
        req.setAttribute("semanas", GeneradorCalendario.semanas(mes, eventos, LocalDate.now()));
        req.setAttribute("totalMes", eventos.stream().filter(e -> YearMonth.from(e.getFechaInicio()).equals(mes)).count());
        req.setAttribute("idRecinto", idRecinto);
        req.setAttribute("mios", mios);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    private static YearMonth leerMes(String valor) {
        if (valor != null) {
            try {
                return YearMonth.parse(valor.trim());
            } catch (DateTimeParseException ex) {
                // Si el parámetro no es válido se muestra el mes actual.
            }
        }
        return YearMonth.now();
    }
}
