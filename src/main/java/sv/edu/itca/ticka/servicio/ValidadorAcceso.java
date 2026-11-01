package sv.edu.itca.ticka.servicio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import sv.edu.itca.ticka.modelo.BoletoPuerta;

/**
 * Reglas de validación de un boleto en puerta (RF-26, RF-30, RF-31, RN-07,
 * RN-08). Sin base de datos, para probarlas con JUnit. Devuelve el motivo
 * del rechazo o null si el boleto es válido.
 */
public final class ValidadorAcceso {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private ValidadorAcceso() {
    }

    /**
     * @param boleto        boleto encontrado por su código (null si no existe)
     * @param idEvento      evento que se está controlando en esta puerta
     * @param apertura      apertura de puertas del evento
     * @param fin           fin del evento
     * @param reentrada     true si se permite volver a entrar (RF-31)
     */
    public static String motivoRechazo(BoletoPuerta boleto, long idEvento, LocalDateTime apertura,
            LocalDateTime fin, LocalDateTime ahora, boolean reentrada) {
        // RF-30: solo se valida desde la apertura de puertas hasta el fin del evento.
        if (ahora.isBefore(apertura)) {
            return "Fuera de horario: las puertas abren a las " + apertura.format(HORA);
        }
        if (ahora.isAfter(fin)) {
            return "Fuera de horario: el evento ya terminó";
        }
        if (boleto == null) {
            return "Código inexistente";
        }
        if (boleto.getIdEvento() != idEvento) {
            return "Boleto de otro evento (" + recortar(boleto.getEvento(), 50) + ")";
        }
        if ("Anulada".equals(boleto.getEstado())) {
            return "Boleto anulado";
        }
        if ("Usada".equals(boleto.getEstado()) && !reentrada) {
            return "Boleto ya utilizado";
        }
        return null;
    }

    private static String recortar(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
