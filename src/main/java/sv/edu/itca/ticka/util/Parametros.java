package sv.edu.itca.ticka.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Conversión segura de parámetros de formulario: devuelve null cuando el
 * campo viene vacío o con un formato inválido, para que el servlet decida
 * qué mensaje mostrar.
 */
public final class Parametros {

    private Parametros() {
    }

    public static String texto(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    public static Integer entero(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static Long enteroLargo(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Long.valueOf(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static BigDecimal decimal(String s) {
        try {
            return (s == null || s.isBlank()) ? null : new BigDecimal(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Lee el formato de &lt;input type="datetime-local"&gt; (2026-11-20T20:00). */
    public static LocalDateTime fechaHora(String s) {
        try {
            return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** Lee el formato de &lt;input type="date"&gt; (2026-11-20). */
    public static LocalDate fecha(String s) {
        try {
            return (s == null || s.isBlank()) ? null : LocalDate.parse(s.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /** true si el campo se escribió pero no se pudo interpretar. */
    public static boolean invalido(String escrito, Object interpretado) {
        return escrito != null && !escrito.isBlank() && interpretado == null;
    }
}
