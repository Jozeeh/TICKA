package sv.edu.itca.ticka.util;

import java.util.List;
import sv.edu.itca.ticka.modelo.Reporte;

/**
 * Convierte un Reporte a CSV (RF-48). Se antepone el BOM de UTF-8 para que
 * Excel abra bien las tildes, y cada valor va entre comillas dobles.
 */
public final class ExportadorCsv {

    /** Marca de orden de bytes UTF-8 que Excel usa para detectar la codificación. */
    public static final String BOM = "﻿";

    private ExportadorCsv() {
    }

    public static String generar(Reporte r) {
        StringBuilder sb = new StringBuilder(BOM);
        agregarLinea(sb, r.getColumnas());
        for (List<String> fila : r.getFilas()) {
            agregarLinea(sb, fila);
        }
        if (r.getTotales() != null) {
            agregarLinea(sb, r.getTotales());
        }
        return sb.toString();
    }

    private static void agregarLinea(StringBuilder sb, List<String> valores) {
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapar(valores.get(i)));
        }
        sb.append("\r\n");
    }

    /**
     * Encierra el valor en comillas y duplica las comillas internas. Si
     * empieza con =, +, - o @ se antepone un apóstrofo para que Excel no lo
     * interprete como fórmula (inyección CSV).
     */
    static String escapar(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
