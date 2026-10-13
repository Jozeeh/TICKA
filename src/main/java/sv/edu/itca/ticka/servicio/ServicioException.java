package sv.edu.itca.ticka.servicio;

/**
 * Error de regla de negocio: su mensaje está pensado para mostrarse
 * directamente al usuario (por ejemplo "El recinto ya tiene otro evento en
 * ese horario").
 */
public class ServicioException extends RuntimeException {

    public ServicioException(String message) {
        super(message);
    }

    public ServicioException(String message, Throwable cause) {
        super(message, cause);
    }
}
