package sv.edu.itca.ticka.servicio;

import java.util.regex.Pattern;
import sv.edu.itca.ticka.modelo.Cliente;

/**
 * Validación de los datos del cliente al registrarse o editar su perfil
 * (RF-13). Devuelve el mensaje del primer error o null si todo es válido.
 */
public final class ValidadorCliente {

    /** DUI (00000000-0) o pasaporte/documento extranjero (5 a 20 letras, números o guiones). */
    private static final Pattern DOCUMENTO = Pattern.compile("^(\\d{8}-\\d|[A-Za-z0-9-]{5,20})$");
    private static final Pattern CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+ -]{8,20}$");

    private ValidadorCliente() {
    }

    public static String validar(Cliente c) {
        if (vacio(c.getNombres()) || c.getNombres().length() > 80) {
            return "Escriba sus nombres (máximo 80 caracteres).";
        }
        if (vacio(c.getApellidos()) || c.getApellidos().length() > 80) {
            return "Escriba sus apellidos (máximo 80 caracteres).";
        }
        if (vacio(c.getDocumento()) || !DOCUMENTO.matcher(c.getDocumento()).matches()) {
            return "El documento debe ser un DUI (00000000-0) o un pasaporte de 5 a 20 caracteres.";
        }
        if (vacio(c.getCorreo()) || c.getCorreo().length() > 120 || !CORREO.matcher(c.getCorreo()).matches()) {
            return "Escriba un correo electrónico válido.";
        }
        if (!vacio(c.getTelefono()) && !TELEFONO.matcher(c.getTelefono()).matches()) {
            return "El teléfono debe tener de 8 a 20 dígitos (puede incluir +, espacios o guiones).";
        }
        return null;
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
