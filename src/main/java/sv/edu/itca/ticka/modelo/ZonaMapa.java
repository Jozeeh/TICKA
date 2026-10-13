package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.ticka.util.Formatos;

/** Zona dentro del mapa de asientos de un evento (con su precio y filas). */
public class ZonaMapa {

    private final Long idZona;
    private final String nombre;
    private BigDecimal precio;
    private final List<FilaAsientos> filas = new ArrayList<>();

    public ZonaMapa(Long idZona, String nombre) {
        this.idZona = idZona;
        this.nombre = nombre;
    }

    public String getPrecioTexto() { return Formatos.dinero(precio); }

    public Long getIdZona() { return idZona; }

    public String getNombre() { return nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public List<FilaAsientos> getFilas() { return filas; }
}
