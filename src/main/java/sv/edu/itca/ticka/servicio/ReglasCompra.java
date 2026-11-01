package sv.edu.itca.ticka.servicio;

import java.time.LocalDateTime;
import sv.edu.itca.ticka.modelo.EstadoEvento;
import sv.edu.itca.ticka.modelo.VentaAnulable;

/**
 * Reglas de la compra y la anulación que no dependen de la base de datos
 * (así se prueban con JUnit). Cada método devuelve el mensaje de error o
 * null si la operación está permitida.
 */
public final class ReglasCompra {

    private ReglasCompra() {
    }

    /** RN-03: solo se vende un evento Publicado y antes de su hora de inicio. */
    public static String validarVentaAbierta(EstadoEvento estado, LocalDateTime inicio, LocalDateTime ahora) {
        if (estado == EstadoEvento.AGOTADO) {
            return "El evento está agotado.";
        }
        if (estado != EstadoEvento.PUBLICADO) {
            return "El evento no está a la venta (estado " + estado.getBd().toLowerCase() + ").";
        }
        if (!ahora.isBefore(inicio)) {
            return "La venta de este evento ya cerró: el evento ya empezó.";
        }
        return null;
    }

    /** RN-04: entre 1 y el máximo de asientos por compra. */
    public static String validarCantidad(int cantidad, int maximo) {
        if (cantidad <= 0) {
            return "Seleccione al menos un asiento.";
        }
        if (cantidad > maximo) {
            return "Puede comprar como máximo " + maximo + " asientos por compra.";
        }
        return null;
    }

    /**
     * RN-12: el cliente anula solo compras pagadas, de eventos que no se han
     * cancelado ni terminado, hasta N horas antes de la apertura de puertas
     * y si ningún boleto se usó.
     */
    public static String motivoNoAnulable(VentaAnulable v, LocalDateTime ahora, int horas) {
        if (!"Pagada".equals(v.getEstado())) {
            return "La compra ya está anulada.";
        }
        if (v.getEstadoEvento() == EstadoEvento.CANCELADO || v.getEstadoEvento() == EstadoEvento.FINALIZADO
                || v.getEstadoEvento() == EstadoEvento.EN_CURSO) {
            return "El evento está " + v.getEstadoEvento().getBd().toLowerCase() + ": la compra ya no se puede anular.";
        }
        if (v.getBoletosUsados() > 0) {
            return "No se puede anular: uno de los boletos ya se usó en puerta.";
        }
        if (!ahora.isBefore(v.getAperturaPuertas().minusHours(horas))) {
            return "Solo se puede anular hasta " + horas + " horas antes de la apertura de puertas.";
        }
        return null;
    }
}
