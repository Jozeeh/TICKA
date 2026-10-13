package sv.edu.itca.ticka.modelo;

import java.util.ArrayList;
import java.util.List;

/** Una fila (A, B, C...) con sus asientos, para dibujar el mapa en la JSP. */
public class FilaAsientos {

    private final String letra;
    private final List<Asiento> asientos = new ArrayList<>();

    public FilaAsientos(String letra) {
        this.letra = letra;
    }

    public String getLetra() { return letra; }

    public List<Asiento> getAsientos() { return asientos; }
}
