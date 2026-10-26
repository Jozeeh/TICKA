package sv.edu.itca.ticka.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.CompraCliente;

/** DAO de consulta de clientes para el Administrador (RF-37, RF-54, RF-55). */
public interface ClienteDAO {

    /**
     * Página de clientes con el resumen de compras vigentes. Si texto no es
     * null, filtra por nombre, documento, correo o usuario.
     */
    List<Cliente> listar(String texto, int desplazamiento, int limite);

    int contar(String texto);

    /** Cliente con su resumen de compras. */
    Optional<Cliente> buscarPorId(long id);

    /** Ventas del cliente (de la más reciente a la más antigua) con sus boletos. */
    List<CompraCliente> compras(long idCliente);
}
