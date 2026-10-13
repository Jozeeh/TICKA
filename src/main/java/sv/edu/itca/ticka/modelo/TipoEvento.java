package sv.edu.itca.ticka.modelo;

/**
 * Tipos de evento. "bd" es el valor del ENUM en evento.tipo (sin tilde) y
 * "etiqueta" el texto que se muestra.
 */
public enum TipoEvento {

    CONCIERTO("Concierto", "Concierto"),
    PARTIDO("Partido", "Partido"),
    OPERA("Opera", "Ópera"),
    TEATRO("Teatro", "Teatro"),
    OTRO("Otro", "Otro");

    private final String bd;
    private final String etiqueta;

    TipoEvento(String bd, String etiqueta) {
        this.bd = bd;
        this.etiqueta = etiqueta;
    }

    public String getBd() {
        return bd;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoEvento desdeBd(String valor) {
        if (valor == null) {
            return null;
        }
        for (TipoEvento t : values()) {
            if (t.bd.equalsIgnoreCase(valor) || t.name().equalsIgnoreCase(valor)) {
                return t;
            }
        }
        return null;
    }
}
