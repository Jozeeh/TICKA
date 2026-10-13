package sv.edu.itca.ticka.servicio;

import java.util.regex.Pattern;

/**
 * Reglas de formato para nombres de usuario y contraseñas. No depende de la
 * base de datos, por eso se prueba con JUnit (ValidadorUsuarioTest).
 */
public final class ValidadorUsuario {

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9._-]{4,30}$");
    private static final Pattern TIENE_LETRA = Pattern.compile(".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*");
    private static final Pattern TIENE_NUMERO = Pattern.compile(".*\\d.*");

    public static final int LONGITUD_MINIMA_CLAVE = 8;

    private ValidadorUsuario() {
    }

    /** Devuelve el mensaje de error o null si el username es válido. */
    public static String validarUsername(String username) {
        if (username == null || username.isBlank()) {
            return "El nombre de usuario es obligatorio.";
        }
        if (!USERNAME.matcher(username).matches()) {
            return "El usuario debe tener de 4 a 30 caracteres: minúsculas, números, punto, guion o guion bajo.";
        }
        return null;
    }

    /** Devuelve el mensaje de error o null si la contraseña cumple la política. */
    public static String validarClave(String clave, String confirmacion) {
        if (clave == null || clave.length() < LONGITUD_MINIMA_CLAVE) {
            return "La contraseña debe tener al menos " + LONGITUD_MINIMA_CLAVE + " caracteres.";
        }
        if (!TIENE_LETRA.matcher(clave).matches() || !TIENE_NUMERO.matcher(clave).matches()) {
            return "La contraseña debe combinar letras y números.";
        }
        if (!clave.equals(confirmacion)) {
            return "La confirmación no coincide con la contraseña.";
        }
        return null;
    }

    /** Devuelve el mensaje de error o null si el nombre es válido. */
    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "El nombre completo es obligatorio.";
        }
        if (nombre.length() > 100) {
            return "El nombre no puede superar los 100 caracteres.";
        }
        return null;
    }
}
