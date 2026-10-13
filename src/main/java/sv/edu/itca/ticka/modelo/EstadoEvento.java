package sv.edu.itca.ticka.modelo;

/**
 * Estados del evento (RF-02). El valor de "bd" es el texto del ENUM en la
 * columna evento.estado.
 *
 * Flujo: Borrador → Publicado ⇄ Agotado → En curso → Finalizado.
 * Desde Borrador, Publicado o Agotado se puede pasar a Cancelado.
 */
public enum EstadoEvento {

    BORRADOR("Borrador"),
    PUBLICADO("Publicado"),
    AGOTADO("Agotado"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String bd;

    EstadoEvento(String bd) {
        this.bd = bd;
    }

    public String getBd() {
        return bd;
    }

    /** Clase CSS de la etiqueta de estado (sin espacios). */
    public String getClase() {
        return bd.replace(" ", "");
    }

    public static EstadoEvento desdeBd(String valor) {
        for (EstadoEvento e : values()) {
            if (e.bd.equalsIgnoreCase(valor)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de evento desconocido: " + valor);
    }

    /** Se pueden modificar sus datos (nombre, fechas, precios...). */
    public boolean isEditable() {
        return this == BORRADOR || this == PUBLICADO || this == AGOTADO;
    }

    /** Ya tiene mapa de asientos generado. */
    public boolean isConMapa() {
        return this != BORRADOR;
    }

    public boolean isCancelable() {
        return this == BORRADOR || this == PUBLICADO || this == AGOTADO;
    }

    /** Siguiente estado manual permitido (null si no hay). */
    public EstadoEvento siguienteManual() {
        switch (this) {
            case PUBLICADO:
            case AGOTADO:
                return EN_CURSO;
            case EN_CURSO:
                return FINALIZADO;
            default:
                return null;
        }
    }
}
