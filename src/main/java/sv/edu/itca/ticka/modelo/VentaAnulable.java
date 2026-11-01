package sv.edu.itca.ticka.modelo;

import java.time.LocalDateTime;

/** Datos de una venta que se necesitan para decidir si se puede anular (RN-12). */
public class VentaAnulable {

    private long id;
    private long idCliente;
    private String estado;
    private long idEvento;
    private EstadoEvento estadoEvento;
    private LocalDateTime aperturaPuertas;
    private int boletosUsados;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getIdCliente() { return idCliente; }
    public void setIdCliente(long idCliente) { this.idCliente = idCliente; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public long getIdEvento() { return idEvento; }
    public void setIdEvento(long idEvento) { this.idEvento = idEvento; }
    public EstadoEvento getEstadoEvento() { return estadoEvento; }
    public void setEstadoEvento(EstadoEvento estadoEvento) { this.estadoEvento = estadoEvento; }
    public LocalDateTime getAperturaPuertas() { return aperturaPuertas; }
    public void setAperturaPuertas(LocalDateTime aperturaPuertas) { this.aperturaPuertas = aperturaPuertas; }
    public int getBoletosUsados() { return boletosUsados; }
    public void setBoletosUsados(int boletosUsados) { this.boletosUsados = boletosUsados; }
}
