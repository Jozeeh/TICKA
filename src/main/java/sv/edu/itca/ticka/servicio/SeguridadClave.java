package sv.edu.itca.ticka.servicio;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Transforma y verifica contraseñas con BCrypt (hash + salt, RF-40).
 *
 * A diferencia del SHA-256 de las guías, BCrypt agrega un salt aleatorio y
 * un factor de costo, así dos contraseñas iguales producen hashes distintos
 * y un ataque de fuerza bruta es mucho más lento.
 */
public final class SeguridadClave {

    private static final int COSTO = 10;

    private SeguridadClave() {
    }

    public static String generarHash(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt(COSTO));
    }

    /**
     * Compara la clave escrita con el hash guardado. Devuelve false si el
     * hash no tiene formato BCrypt (por ejemplo las cuentas de demostración
     * con '!', que nunca pueden iniciar sesión).
     */
    public static boolean verificar(String clave, String hash) {
        if (clave == null || hash == null || !hash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(clave, hash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
