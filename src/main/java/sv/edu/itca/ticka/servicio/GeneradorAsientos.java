package sv.edu.itca.ticka.servicio;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcula las filas a generar en una zona (RF-12). Por ejemplo, de la fila
 * "A" a la "J" con 20 asientos por fila produce A..J (10 filas) y 200
 * asientos. No depende de la base de datos (GeneradorAsientosTest).
 */
public final class GeneradorAsientos {

    public static final int MAX_POR_FILA = 50;
    public static final int MAX_ASIENTOS = 1000;

    private GeneradorAsientos() {
    }

    /**
     * Devuelve la lista de letras de fila entre desde y hasta (inclusive).
     *
     * @throws ServicioException si los datos no son válidos
     */
    public static List<String> calcularFilas(String desde, String hasta, Integer porFila) {
        char inicio = letra(desde, "inicial");
        char fin = letra(hasta, "final");
        if (inicio > fin) {
            throw new ServicioException("La fila inicial debe ir antes que la final (por ejemplo, de A a J).");
        }
        if (porFila == null || porFila < 1 || porFila > MAX_POR_FILA) {
            throw new ServicioException("Los asientos por fila deben estar entre 1 y " + MAX_POR_FILA + ".");
        }
        int total = (fin - inicio + 1) * porFila;
        if (total > MAX_ASIENTOS) {
            throw new ServicioException("No se pueden generar más de " + MAX_ASIENTOS
                    + " asientos a la vez (se pidieron " + total + ").");
        }
        List<String> filas = new ArrayList<>();
        for (char c = inicio; c <= fin; c++) {
            filas.add(String.valueOf(c));
        }
        return filas;
    }

    private static char letra(String valor, String nombre) {
        if (valor == null || valor.trim().length() != 1) {
            throw new ServicioException("Escriba una sola letra para la fila " + nombre + ".");
        }
        char c = Character.toUpperCase(valor.trim().charAt(0));
        if (c < 'A' || c > 'Z') {
            throw new ServicioException("La fila " + nombre + " debe ser una letra de la A a la Z.");
        }
        return c;
    }
}
