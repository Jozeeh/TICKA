package sv.edu.itca.ticka.modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resultado tabular de un reporte (RF-43 a RF-46): título, encabezados y
 * filas ya formateadas como texto. La misma estructura se muestra en la JSP
 * y se exporta a CSV (RF-47), así no se duplica la lógica de cada reporte.
 */
public class Reporte {

    private final String titulo;
    private final List<String> columnas;
    private final Set<Integer> numericas = new HashSet<>();
    private final List<List<String>> filas = new ArrayList<>();
    private List<String> totales;
    /** Cifras de resumen (nombre, valor) que se muestran antes de la tabla. */
    private final List<String[]> indicadores = new ArrayList<>();
    /** true si la primera columna es una sección (el PDF agrupa las filas por ella). */
    private boolean agrupado;
    /** Nota aclaratoria para la sección "Información adicional" del PDF. */
    private String nota;

    public Reporte(String titulo, String... columnas) {
        this.titulo = titulo;
        this.columnas = Arrays.asList(columnas);
    }

    /** Marca columnas numéricas (se alinean a la derecha). */
    public Reporte numericas(int... indices) {
        for (int i : indices) {
            numericas.add(i);
        }
        return this;
    }

    public void agregarFila(Object... valores) {
        List<String> fila = new ArrayList<>();
        for (Object v : valores) {
            fila.add(v == null ? "" : String.valueOf(v));
        }
        filas.add(fila);
    }

    public void setTotales(Object... valores) {
        totales = new ArrayList<>();
        for (Object v : valores) {
            totales.add(v == null ? "" : String.valueOf(v));
        }
    }

    public Reporte indicador(String nombre, Object valor) {
        indicadores.add(new String[]{nombre, valor == null ? "" : String.valueOf(valor)});
        return this;
    }

    public boolean esNumerica(int indice) {
        return numericas.contains(indice);
    }

    public String getTitulo() { return titulo; }
    public List<String> getColumnas() { return columnas; }
    public Set<Integer> getNumericas() { return numericas; }
    public List<List<String>> getFilas() { return filas; }
    public List<String> getTotales() { return totales; }
    public List<String[]> getIndicadores() { return indicadores; }
    public boolean isAgrupado() { return agrupado; }
    public void setAgrupado(boolean agrupado) { this.agrupado = agrupado; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}
