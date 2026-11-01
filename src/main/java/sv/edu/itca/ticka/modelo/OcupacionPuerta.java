package sv.edu.itca.ticka.modelo;

import sv.edu.itca.ticka.util.Formatos;

/** Ocupación en vivo de una zona: boletos vendidos frente a ingresados (RF-28). */
public class OcupacionPuerta {

    private final String zona;
    private final int vendidos;
    private final int ingresados;

    public OcupacionPuerta(String zona, int vendidos, int ingresados) {
        this.zona = zona;
        this.vendidos = vendidos;
        this.ingresados = ingresados;
    }

    public int getPorcentaje() { return Formatos.porcentaje(ingresados, vendidos); }
    public int getPendientes() { return Math.max(0, vendidos - ingresados); }

    public String getZona() { return zona; }
    public int getVendidos() { return vendidos; }
    public int getIngresados() { return ingresados; }
}
