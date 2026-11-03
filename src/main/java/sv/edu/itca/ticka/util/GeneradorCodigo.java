package sv.edu.itca.ticka.util;

import java.security.SecureRandom;

/**
 * Código único de cada boleto (RF-20), que también es el contenido del QR.
 * Formato corto TK-XXXX, fácil de leer y de escribir en puerta.
 *
 * - Usa SecureRandom, así el código no se puede deducir a partir de otro (RNF-04).
 * - El alfabeto no tiene caracteres que se confunden (0/O, 1/I): 32 símbolos,
 *   unas 1 048 576 combinaciones.
 * - Con un código tan corto pueden salir repetidos: la columna entrada.codigo
 *   es UNIQUE y CompraServicio genera otro si el INSERT lo rechaza.
 */
public final class GeneradorCodigo {

    public static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int LONGITUD = 4;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeneradorCodigo() {
    }

    public static String nuevo() {
        StringBuilder sb = new StringBuilder("TK-");
        for (int i = 0; i < LONGITUD; i++) {
            sb.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }

    /** Normaliza lo que escribe o escanea el personal de puerta (espacios y minúsculas). */
    public static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
