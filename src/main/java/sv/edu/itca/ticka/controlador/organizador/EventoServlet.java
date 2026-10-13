package sv.edu.itca.ticka.controlador.organizador;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.AsientoEventoDAO;
import sv.edu.itca.ticka.dao.CategoriaDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.EventoDAO;
import sv.edu.itca.ticka.dao.RecintoDAO;
import sv.edu.itca.ticka.dao.impl.AsientoEventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.CategoriaDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.RecintoDAOImpl;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.Evento;
import sv.edu.itca.ticka.modelo.FiltroEvento;
import sv.edu.itca.ticka.modelo.PrecioZona;
import sv.edu.itca.ticka.modelo.TipoEvento;
import sv.edu.itca.ticka.servicio.EventoServicio;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Eventos del Organizador (RF-01 a RF-07, RN-05, RN-06, RN-09).
 *
 * GET:  listar (filtros y paginación), nuevo, editar, ver (detalle con precios).
 * POST: guardar, precios, publicar, avanzar (siguiente estado), cancelar.
 * Las reglas de negocio están en EventoServicio; el servlet solo lee el
 * formulario, delega y decide a qué vista ir.
 */
@WebServlet("/organizador/eventos")
public class EventoServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/vistas/organizador/eventos/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/organizador/eventos/formulario.jsp";
    private static final String VISTA_DETALLE = "/WEB-INF/vistas/organizador/eventos/detalle.jsp";

    private EventoDAO eventoDAO;
    private RecintoDAO recintoDAO;
    private CategoriaDAO categoriaDAO;
    private AsientoEventoDAO asientoEventoDAO;
    private EventoServicio eventoServicio;

    @Override
    public void init() {
        eventoDAO = new EventoDAOImpl();
        recintoDAO = new RecintoDAOImpl();
        categoriaDAO = new CategoriaDAOImpl();
        asientoEventoDAO = new AsientoEventoDAOImpl();
        eventoServicio = new EventoServicio(eventoDAO, recintoDAO, asientoEventoDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            switch (accion == null ? "" : accion) {
                case "nuevo":
                    mostrarFormulario(req, resp, new Evento());
                    break;
                case "editar":
                    Evento editar = buscar(req);
                    if (editar == null) {
                        volverAlListado(req, resp, "El evento no existe.");
                    } else if (!editar.getEstado().isEditable()) {
                        Sesion.error(req, "Un evento " + editar.getEstado().getBd().toLowerCase() + " ya no se puede editar.");
                        resp.sendRedirect(req.getContextPath() + "/organizador/eventos?accion=ver&id=" + editar.getId());
                    } else {
                        mostrarFormulario(req, resp, editar);
                    }
                    break;
                case "ver":
                    Evento ver = buscar(req);
                    if (ver == null) {
                        volverAlListado(req, resp, "El evento no existe.");
                    } else {
                        mostrarDetalle(req, resp, ver);
                    }
                    break;
                default:
                    listar(req, resp);
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar los eventos. Verifique la conexión con MySQL.");
            req.setAttribute("eventos", Collections.emptyList());
            req.setAttribute("filtro", new FiltroEvento());
            req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
        }
    }

    private Evento buscar(HttpServletRequest req) {
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        return id == null ? null : eventoDAO.buscarPorId(id).orElse(null);
    }

    private void volverAlListado(HttpServletRequest req, HttpServletResponse resp, String mensaje)
            throws IOException {
        Sesion.error(req, mensaje);
        resp.sendRedirect(req.getContextPath() + "/organizador/eventos");
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        FiltroEvento f = new FiltroEvento();
        f.setTexto(Parametros.texto(req.getParameter("texto")));
        f.setTipo(TipoEvento.desdeBd(req.getParameter("tipo")));
        String estado = Parametros.texto(req.getParameter("estado"));
        if (estado != null) {
            try {
                f.setEstado(EstadoEvento.valueOf(estado));
            } catch (IllegalArgumentException ex) {
                f.setEstado(null);
            }
        }
        f.setIdRecinto(Parametros.enteroLargo(req.getParameter("recinto")));
        f.setDesde(Parametros.fecha(req.getParameter("desde")));
        f.setHasta(Parametros.fecha(req.getParameter("hasta")));
        Integer pagina = Parametros.entero(req.getParameter("pagina"));
        f.setPagina(pagina == null ? 1 : pagina);

        int total = eventoDAO.contar(f);
        int paginas = Math.max(1, (int) Math.ceil(total / (double) FiltroEvento.TAMANO_PAGINA));
        if (f.getPagina() > paginas) {
            f.setPagina(paginas);
        }
        req.setAttribute("filtro", f);
        req.setAttribute("eventos", eventoDAO.listar(f));
        req.setAttribute("total", total);
        req.setAttribute("paginas", paginas);
        cargarCatalogos(req);
        req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
    }

    private void cargarCatalogos(HttpServletRequest req) {
        req.setAttribute("tipos", TipoEvento.values());
        req.setAttribute("estados", EstadoEvento.values());
        req.setAttribute("recintos", recintoDAO.listar());
        req.setAttribute("categorias", categoriaDAO.listar());
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Evento e)
            throws ServletException, IOException {
        req.setAttribute("evento", e);
        cargarCatalogos(req);
        // Para nuevos eventos solo se ofrecen recintos activos.
        req.setAttribute("recintos", e.getId() == null ? recintoDAO.listarActivos() : recintoDAO.listar());
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    private void mostrarDetalle(HttpServletRequest req, HttpServletResponse resp, Evento e)
            throws ServletException, IOException {
        req.setAttribute("evento", e);
        req.setAttribute("precios", asientoEventoDAO.listarPrecios(e.getId(), e.getIdRecinto()));
        req.setAttribute("ocupacion", asientoEventoDAO.ocupacionPorEvento(e.getId()));
        req.setAttribute("siguienteEstado", e.getEstado().siguienteManual());
        req.getRequestDispatcher(VISTA_DETALLE).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        try {
            switch (accion == null ? "" : accion) {
                case "guardar":
                    guardar(req, resp);
                    return;
                case "precios":
                    guardarPrecios(req, resp, id);
                    return;
                case "publicar":
                    int asientos = eventoServicio.publicar(requerido(id));
                    Sesion.exito(req, "Evento publicado: " + asientos + " asientos habilitados para la venta.");
                    break;
                case "avanzar":
                    EstadoEvento nuevo = eventoServicio.avanzarEstado(requerido(id));
                    Sesion.exito(req, "El evento pasó a estado " + nuevo.getBd() + ".");
                    break;
                case "cancelar":
                    int anulados = eventoServicio.cancelar(requerido(id));
                    Sesion.exito(req, "Evento cancelado. Boletos anulados (reembolso simulado): " + anulados + ".");
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
        resp.sendRedirect(req.getContextPath() + "/organizador/eventos?accion=ver&id=" + id);
    }

    private static long requerido(Long id) {
        if (id == null) {
            throw new ServicioException("Evento no válido.");
        }
        return id;
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Evento e = leerFormulario(req);
        String error = null;
        if (Parametros.invalido(req.getParameter("aperturaPuertas"), e.getAperturaPuertas())
                || Parametros.invalido(req.getParameter("fechaInicio"), e.getFechaInicio())
                || Parametros.invalido(req.getParameter("fechaFin"), e.getFechaFin())) {
            error = "Revise el formato de las fechas y horas.";
        }
        try {
            if (error == null) {
                boolean esNuevo = e.getId() == null;
                long id = eventoServicio.guardar(e, Sesion.usuario(req).getId());
                Sesion.exito(req, esNuevo
                        ? "Evento registrado en Borrador. Defina los precios por zona y publíquelo."
                        : "Evento actualizado.");
                resp.sendRedirect(req.getContextPath() + "/organizador/eventos?accion=ver&id=" + id);
                return;
            }
        } catch (ServicioException ex) {
            error = ex.getMessage();
        }
        // Se conserva el estado real del evento para que la vista muestre lo correcto.
        if (e.getId() != null) {
            eventoDAO.buscarPorId(e.getId()).ifPresent(actual -> e.setEstado(actual.getEstado()));
        }
        req.setAttribute("error", error);
        mostrarFormulario(req, resp, e);
    }

    private Evento leerFormulario(HttpServletRequest req) {
        Evento e = new Evento();
        e.setId(Parametros.enteroLargo(req.getParameter("id")));
        e.setNombre(Parametros.texto(req.getParameter("nombre")));
        e.setTipo(TipoEvento.desdeBd(req.getParameter("tipo")));
        e.setDescripcion(Parametros.texto(req.getParameter("descripcion")));
        e.setParticipantes(Parametros.texto(req.getParameter("participantes")));
        e.setImagen(Parametros.texto(req.getParameter("imagen")));
        e.setIdCategoria(Parametros.enteroLargo(req.getParameter("categoria")));
        e.setIdRecinto(Parametros.enteroLargo(req.getParameter("recinto")));
        e.setAperturaPuertas(Parametros.fechaHora(req.getParameter("aperturaPuertas")));
        e.setFechaInicio(Parametros.fechaHora(req.getParameter("fechaInicio")));
        e.setFechaFin(Parametros.fechaHora(req.getParameter("fechaFin")));
        return e;
    }

    /** Lee los campos precio_{idZona} del formulario de precios. */
    private void guardarPrecios(HttpServletRequest req, HttpServletResponse resp, Long id)
            throws IOException {
        try {
            Evento e = eventoDAO.buscarPorId(requerido(id))
                    .orElseThrow(() -> new ServicioException("El evento no existe."));
            Map<Long, BigDecimal> precios = new LinkedHashMap<>();
            for (PrecioZona p : asientoEventoDAO.listarPrecios(e.getId(), e.getIdRecinto())) {
                String escrito = req.getParameter("precio_" + p.getIdZona());
                BigDecimal precio = Parametros.decimal(escrito);
                if (Parametros.invalido(escrito, precio)) {
                    throw new ServicioException("El precio de la zona " + p.getZona() + " no es un número válido.");
                }
                if (precio != null) {
                    precios.put(p.getIdZona(), precio);
                }
            }
            eventoServicio.guardarPrecios(e.getId(), precios);
            Sesion.exito(req, "Precios guardados.");
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo guardar los precios.");
        }
        resp.sendRedirect(req.getContextPath() + "/organizador/eventos?accion=ver&id=" + id);
    }
}
