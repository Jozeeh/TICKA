package sv.edu.itca.ticka.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.CompraCliente;
import sv.edu.itca.ticka.modelo.Usuario;

/** DAO de clientes: consulta del Administrador (RF-37, RF-54, RF-55) y portal del cliente (RF-13, RF-22). */
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

    /** Cliente de una cuenta de usuario (portal del cliente). */
    Optional<Cliente> buscarPorUsuario(long idUsuario);

    /** Cliente por su documento (venta en mostrador y búsqueda en puerta). */
    Optional<Cliente> buscarPorDocumento(String documento);

    /** RF-14: true si otro cliente (distinto de excluirId) ya usa el documento. */
    boolean existeDocumento(String documento, Long excluirId);

    /** RF-14: true si otro cliente (distinto de excluirId) ya usa el correo. */
    boolean existeCorreo(String correo, Long excluirId);

    /**
     * Autorregistro (RF-13): crea el usuario con rol Cliente y su fila en
     * cliente en una sola transacción. Devuelve el id del cliente.
     */
    long registrar(Usuario usuario, Cliente cliente);

    /** Actualiza nombres, apellidos, correo y teléfono (y el nombre del usuario). */
    void actualizarPerfil(Cliente cliente);
}
