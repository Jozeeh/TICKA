package sv.edu.itca.ticka.modelo;

/** Boleto leído en puerta: su estado, evento, asiento y a quién pertenece (RF-25, RF-27). */
public class BoletoPuerta {

    private long idEntrada;
    private String codigo;
    private String estado;
    private long idEvento;
    private String evento;
    private String zona;
    private String fila;
    private int numero;
    private String cliente;
    private String documento;

    public String getAsiento() { return fila + "-" + numero; }

    public long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(long idEntrada) { this.idEntrada = idEntrada; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public long getIdEvento() { return idEvento; }
    public void setIdEvento(long idEvento) { this.idEvento = idEvento; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public String getFila() { return fila; }
    public void setFila(String fila) { this.fila = fila; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
}
