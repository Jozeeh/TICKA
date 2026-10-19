package sv.edu.itca.ticka.dao;

import java.util.List;
import sv.edu.itca.ticka.modelo.Cliente;

/** DAO de consulta de clientes para el Administrador (RF-37). */
public interface ClienteDAO {

    /**
     * Clientes con el resumen de compras vigentes. Si texto no es null,
     * filtra por nombre, documento, correo o usuario.
     */
    List<Cliente> listar(String texto);
}
