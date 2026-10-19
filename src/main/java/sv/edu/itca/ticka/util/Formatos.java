package sv.edu.itca.ticka.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos de presentación compartidos por los modelos (las JSP con JSTL no
 * formatean LocalDateTime directamente).
 */
public final class Formatos {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    /** Formato que espera &lt;input type="datetime-local"&gt;. */
    private static final DateTimeFormatter ENTRADA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private Formatos() {
    }

    public static String fechaHora(LocalDateTime f) {
        return f == null ? "" : f.format(FECHA_HORA);
    }

    public static String fecha(LocalDateTime f) {
        return f == null ? "" : f.format(FECHA);
    }

    public static String hora(LocalDateTime f) {
        return f == null ? "" : f.format(HORA);
    }

    public static String entrada(LocalDateTime f) {
        return f == null ? "" : f.format(ENTRADA);
    }

    private static final String[] MESES = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
        "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};
    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    /** Mes abreviado en español: "NOV". */
    public static String mesCorto(LocalDateTime f) {
        return f == null ? "" : MESES[f.getMonthValue() - 1];
    }

    private static final String[] MESES_LARGOS = {"enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    /** Fecha larga en español: "Martes 17 de noviembre de 2026". */
    public static String fechaLarga(LocalDateTime f) {
        return f == null ? "" : diaSemana(f) + " " + f.getDayOfMonth() + " de "
                + MESES_LARGOS[f.getMonthValue() - 1] + " de " + f.getYear();
    }

    /** Día de la semana en español: "Viernes". */
    public static String diaSemana(LocalDateTime f) {
        return f == null ? "" : DIAS[f.getDayOfWeek().getValue() - 1];
    }

    /** Montos en dólares: $1,234.50 */
    public static String dinero(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("$#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(monto);
    }

    /** Porcentaje entero de parte/total (0 si el total es 0). */
    public static int porcentaje(long parte, long total) {
        return total <= 0 ? 0 : (int) Math.round(parte * 100.0 / total);
    }
}
