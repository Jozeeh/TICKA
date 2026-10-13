package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import sv.edu.itca.ticka.util.Formatos;

/**
 * Precio de una zona del recinto para un evento (tabla precio_zona). Si la
 * zona todavía no tiene precio, "precio" es null.
 */
public class PrecioZona {

    private Long idZona;
    private String zona;
    private int asientosActivos;
    private BigDecimal precio;

    public String getPrecioTexto() { return Formatos.dinero(precio); }

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public int getAsientosActivos() { return asientosActivos; }
    public void setAsientosActivos(int asientosActivos) { this.asientosActivos = asientosActivos; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
