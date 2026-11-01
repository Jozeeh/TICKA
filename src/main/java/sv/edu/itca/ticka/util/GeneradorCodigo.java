package sv.edu.itca.ticka.util;

import java.security.SecureRandom;

/**
 * Código único de cada boleto (RF-20), que también es el contenido del QR.
 * Usa SecureRandom (128 bits) para que no se pueda adivinar ni deducir el
 * código de otro boleto (RNF-04). Formato: TK- más 32 caracteres hexadecimales.
 */
public final class GeneradorCodigo {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private GeneradorCodigo() {
    }

    public static String nuevo() {
        byte[] bytes = new byte[16];
        ALEATORIO.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("TK-");
        for (byte b : bytes) {
            sb.append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
        }
        return sb.toString();
    }

    /** Normaliza lo que escribe o escanea el personal de puerta (espacios y minúsculas). */
    public static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
