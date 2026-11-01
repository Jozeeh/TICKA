package sv.edu.itca.ticka.controlador.cliente;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.VentaDAO;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.EventoDAOImpl;
import sv.edu.itca.ticka.dao.impl.VentaDAOImpl;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.CompraCliente;
import sv.edu.itca.ticka.servicio.CompraServicio;
import sv.edu.itca.ticka.servicio.ReglasCompra;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.util.ComprobantePdf;
import sv.edu.itca.ticka.util.Configuracion;
import sv.edu.itca.ticka.util.GeneradorQr;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * "Mis boletos" del cliente (RF-22, RF-23, RN-12, RN-13):
 *   GET                  historial de compras con cada boleto, su estado y su QR.
 *   GET ?qr=ID           imagen PNG del QR de un boleto propio.
 *   GET ?comprobante=ID  comprobante PDF de una compra propia (se abre en el navegador).
 *   POST accion=anular   anula una compra propia dentro del plazo, con motivo.
 * Todo se consulta filtrando por el cliente en sesión: cambiar un id en la URL
 * no da acceso a boletos ajenos.
 */
@WebServlet("/cliente/boletos")
public class BoletosServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/cliente/boletos.jsp";

    private ClienteDAO clienteDAO;
    private VentaDAO ventaDAO;
    private CompraServicio compraServicio;

    @Override
    public void init() {
        clienteDAO = new ClienteDAOImpl();
        ventaDAO = new VentaDAOImpl();
        compraServicio = new CompraServicio(new EventoDAOImpl(), ventaDAO);
    }

    private Cliente cliente(HttpServletRequest req) {
        return clienteDAO.buscarPorUsuario(Sesion.usuario(req).getId())
                .orElseThrow(() -> new ServicioException("Su cuenta no tiene datos de cliente."));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Cliente cliente = cliente(req);
            Long qr = Parametros.enteroLargo(req.getParameter("qr"));
            if (qr != null) {
                enviarQr(resp, cliente, qr);
                return;
            }
            Long comprobante = Parametros.enteroLargo(req.getParameter("comprobante"));
            if (comprobante != null) {
                enviarComprobante(resp, cliente, comprobante);
                return;
            }
            List<CompraCliente> compras = clienteDAO.compras(cliente.getId());
            LocalDateTime ahora = LocalDateTime.now();
            int horas = Configuracion.horasAnulacion();
            for (CompraCliente c : compras) {
                c.setMotivoNoAnulable(ReglasCompra.motivoNoAnulable(c.comoAnulable(), ahora, horas));
            }
            req.setAttribute("cliente", cliente);
            req.setAttribute("compras", compras);
            req.setAttribute("horasAnulacion", horas);
        } catch (ServicioException ex) {
            req.setAttribute("error", ex.getMessage());
            req.setAttribute("compras", Collections.emptyList());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar sus boletos. Intente de nuevo.");
            req.setAttribute("compras", Collections.emptyList());
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    /** QR del boleto (RF-20), solo si el boleto es del cliente en sesión (RN-13). */
    private void enviarQr(HttpServletResponse resp, Cliente cliente, long idEntrada) throws IOException {
        Optional<String> codigo = ventaDAO.codigoDeBoleto(idEntrada, cliente.getId());
        if (codigo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] png = GeneradorQr.png(codigo.get(), 240);
        resp.setContentType("image/png");
        resp.setHeader("Cache-Control", "private, no-store");
        resp.setContentLength(png.length);
        resp.getOutputStream().write(png);
    }

    /** Comprobante PDF de una compra propia (RF-22). */
    private void enviarComprobante(HttpServletResponse resp, Cliente cliente, long idVenta) throws IOException {
        Optional<CompraCliente> compra = clienteDAO.compras(cliente.getId()).stream()
                .filter(c -> c.getId() == idVenta).findFirst();
        if (compra.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] pdf = ComprobantePdf.generar(compra.get(), cliente);
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"ticka_comprobante_" + idVenta + ".pdf\"");
        resp.setHeader("Cache-Control", "private, no-store");
        resp.setContentLength(pdf.length);
        resp.getOutputStream().write(pdf);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long idVenta = Parametros.enteroLargo(req.getParameter("venta"));
        if (!"anular".equals(req.getParameter("accion")) || idVenta == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            compraServicio.anular(idVenta, cliente(req).getId(), req.getParameter("motivo"));
            Sesion.exito(req, "Compra anulada. Los asientos se liberaron y el reembolso se registró (simulado).");
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo anular la compra.");
        }
        resp.sendRedirect(req.getContextPath() + "/cliente/boletos");
    }
}
