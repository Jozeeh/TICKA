package sv.edu.itca.ticka.servicio;

/**
 * Valida el afiche adjunto de un evento. El tipo se detecta por los primeros
 * bytes del archivo (su "firma"), no por la extensión ni por lo que dice el
 * navegador, para que no se pueda subir otro tipo de archivo renombrado
 * como imagen. Se prueba con ValidadorImagenTest.
 */
public final class ValidadorImagen {

    /** Tamaño máximo del afiche: 2 MB. */
    public static final int TAMANO_MAXIMO = 2 * 1024 * 1024;

    private ValidadorImagen() {
    }

    /**
     * Devuelve el tipo MIME de la imagen o lanza ServicioException si el
     * archivo está vacío, es muy grande o no es JPG, PNG, WEBP ni GIF.
     */
    public static String validar(byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new ServicioException("El archivo del afiche está vacío.");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new ServicioException("El afiche no puede pesar más de 2 MB.");
        }
        String tipo = detectarTipo(contenido);
        if (tipo == null) {
            throw new ServicioException("El afiche debe ser una imagen JPG, PNG, WEBP o GIF.");
        }
        return tipo;
    }

    static String detectarTipo(byte[] b) {
        if (empiezaCon(b, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (empiezaCon(b, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (empiezaCon(b, 'G', 'I', 'F', '8')) {
            return "image/gif";
        }
        if (b.length >= 12 && empiezaCon(b, 'R', 'I', 'F', 'F')
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static boolean empiezaCon(byte[] b, int... firma) {
        if (b.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((b[i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
