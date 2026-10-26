package sv.edu.itca.ticka.util;

import java.util.Map;

/**
 * JSON mínimo para pasar series a los gráficos del dashboard (RF-53) sin
 * agregar una librería. El texto se escapa también para '<', '>' y '&', así
 * es seguro incrustarlo dentro de una etiqueta script de la JSP.
 */
public final class Json {

    private Json() {
    }

    /** {"etiquetas":[...],"valores":[...]} conservando el orden del mapa. */
    public static String serie(Map<String, ? extends Number> datos) {
        StringBuilder etiquetas = new StringBuilder();
        StringBuilder valores = new StringBuilder();
        for (Map.Entry<String, ? extends Number> e : datos.entrySet()) {
            if (etiquetas.length() > 0) {
                etiquetas.append(',');
                valores.append(',');
            }
            etiquetas.append(texto(e.getKey()));
            valores.append(e.getValue() == null ? "0" : e.getValue().toString());
        }
        return "{\"etiquetas\":[" + etiquetas + "],\"valores\":[" + valores + "]}";
    }

    /** Cadena JSON entre comillas, con los caracteres especiales escapados. */
    public static String texto(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : (s == null ? "" : s).toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '<':
                case '>':
                case '&':
                    sb.append(String.format("\\u%04x", (int) c));
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
