package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import sv.edu.itca.ticka.util.Formatos;

/**
 * Asiento de un evento elegido para comprar (reservado por el cliente o
 * elegido en mostrador), con su zona y precio (RF-17, RF-18).
 */
public class AsientoSeleccionado {

    private long idEventoAsiento;
    private String zona;
    private String fila;
    private int numero;
    private BigDecimal precio = BigDecimal.ZERO;
    private LocalDateTime reservadoHasta;

    public String getEtiqueta() { return zona + " " + fila + "-" + numero; }
    public String getPrecioTexto() { return Formatos.dinero(precio); }

    public long getIdEventoAsiento() { return idEventoAsiento; }
    public void setIdEventoAsiento(long idEventoAsiento) { this.idEventoAsiento = idEventoAsiento; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getFila() { return fila; }
    public void setFila(String fila) { this.fila = fila; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public LocalDateTime getReservadoHasta() { return reservadoHasta; }
    public void setReservadoHasta(LocalDateTime reservadoHasta) { this.reservadoHasta = reservadoHasta; }
}
