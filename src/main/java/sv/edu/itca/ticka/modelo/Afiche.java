package sv.edu.itca.ticka.modelo;

/** Imagen adjunta de un evento (columnas evento.afiche y evento.afiche_tipo). */
public class Afiche {

    private final byte[] contenido;
    private final String tipo;

    public Afiche(byte[] contenido, String tipo) {
        this.contenido = contenido;
        this.tipo = tipo;
    }

    public byte[] getContenido() { return contenido; }

    /** Tipo MIME: image/jpeg, image/png, image/webp o image/gif. */
    public String getTipo() { return tipo; }
}
