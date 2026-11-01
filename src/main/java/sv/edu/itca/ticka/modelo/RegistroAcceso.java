package sv.edu.itca.ticka.modelo;

import java.time.LocalDateTime;
import sv.edu.itca.ticka.util.Formatos;

/** Un registro de la tabla acceso para la bitácora de la puerta (RF-27, RF-29). */
public class RegistroAcceso {

    private long id;
    private Long idEntrada;
    private long idEvento;
    private LocalDateTime fechaHora;
    private String codigo;
    private String puerta;
    private String resultado;
    private String motivoRechazo;
    private boolean anulado;
    private String motivoAnulacion;
    private String asiento;
    private String validador;

    public String getHoraTexto() { return Formatos.hora(fechaHora); }
    public boolean isValido() { return "Valido".equals(resultado); }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Long idEntrada) { this.idEntrada = idEntrada; }
    public long getIdEvento() { return idEvento; }
    public void setIdEvento(long idEvento) { this.idEvento = idEvento; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    public boolean isAnulado() { return anulado; }
    public void setAnulado(boolean anulado) { this.anulado = anulado; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }
    public String getAsiento() { return asiento; }
    public void setAsiento(String asiento) { this.asiento = asiento; }
    public String getValidador() { return validador; }
    public void setValidador(String validador) { this.validador = validador; }
}
