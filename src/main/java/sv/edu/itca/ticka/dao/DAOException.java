package sv.edu.itca.ticka.dao;

/**
 * Excepción propia de la capa de persistencia: registra la causa técnica
 * (SQLException) sin que el Servlet o la JSP dependan de detalles JDBC.
 */
public class DAOException extends RuntimeException {

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(String message) {
        super(message);
    }
}
