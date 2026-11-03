package sv.edu.itca.ticka.controlador.cliente;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.VentaDAO;
import sv.edu.itca.ticka.dao.impl.ClienteDAOImpl;
import sv.edu.itca.ticka.dao.impl.VentaDAOImpl;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Carrito de la barra superior: busca en la base de datos la reserva vigente del
 * cliente (aunque haya iniciado sesión de nuevo) y lo lleva al pago de esa reserva.
 * Si no tiene asientos reservados lo lleva a la cartelera.
 */
@WebServlet("/cliente/carrito")
public class CarritoServlet extends HttpServlet {

    private static final ClienteDAO CLIENTES = new ClienteDAOImpl();
    private static final VentaDAO VENTAS = new VentaDAOImpl();

    /**
     * Actualiza el contador del carrito con la reserva vigente del cliente y
     * devuelve el evento de esa reserva (vacío si no tiene).
     */
    public static Optional<Long> cargar(HttpServletRequest req, long idUsuario) {
        Optional<Cliente> cliente = CLIENTES.buscarPorUsuario(idUsuario);
        Optional<Long> evento = cliente.flatMap(c -> VENTAS.eventoConReserva(c.getId()));
        if (evento.isPresent()) {
            Sesion.carrito(req, evento.get(), VENTAS.reservados(evento.get(), cliente.get().getId()).size());
        } else {
            Sesion.limpiarCarrito(req);
        }
        return evento;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Optional<Long> evento = cargar(req, Sesion.usuario(req).getId());
            if (evento.isPresent()) {
                resp.sendRedirect(req.getContextPath() + "/cliente/pago?evento=" + evento.get());
                return;
            }
            Sesion.exito(req, "Su carrito está vacío. Elija un evento y sus asientos.");
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo consultar su carrito. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/cliente/cartelera");
    }
}
