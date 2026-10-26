package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.ticka.util.Formatos;

/** Una venta de un cliente con sus boletos, para el detalle del cliente (RF-54). Solo lectura. */
public class CompraCliente {

    private long id;
    private LocalDateTime fecha;
    private String canal;
    private String formaPago;
    private String estado;
    private BigDecimal total = BigDecimal.ZERO;
    private String motivoAnulacion;
    private final List<Boleto> boletos = new ArrayList<>();

    /** Boleto de la compra: evento, asiento, precio y estado. */
    public static class Boleto {
        private String evento;
        private LocalDateTime fechaEvento;
        private String zona;
        private String fila;
        private int numero;
        private BigDecimal precio = BigDecimal.ZERO;
        private String estado;
        private String codigo;

        public String getFechaEventoTexto() { return Formatos.fechaHora(fechaEvento); }
        public String getPrecioTexto() { return Formatos.dinero(precio); }
        public String getAsiento() { return fila + "-" + numero; }
        /** Solo el inicio del código (la parte que cambia en cada boleto); el código completo es el QR del cliente. */
        public String getCodigoCorto() {
            return codigo == null || codigo.length() <= 11 ? codigo : codigo.substring(0, 11) + "…";
        }

        public String getEvento() { return evento; }
        public void setEvento(String evento) { this.evento = evento; }
        public LocalDateTime getFechaEvento() { return fechaEvento; }
        public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }
        public String getZona() { return zona; }
        public void setZona(String zona) { this.zona = zona; }
        public String getFila() { return fila; }
        public void setFila(String fila) { this.fila = fila; }
        public int getNumero() { return numero; }
        public void setNumero(int numero) { this.numero = numero; }
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
    }

    public String getFechaTexto() { return Formatos.fechaHora(fecha); }
    public String getTotalTexto() { return Formatos.dinero(total); }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }
    public List<Boleto> getBoletos() { return boletos; }
}
