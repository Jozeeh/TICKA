package sv.edu.itca.ticka.modelo;

import java.time.LocalDateTime;
import sv.edu.itca.ticka.util.Formatos;

/** Incidencia registrada durante un evento (M5, RF-32 a RF-35). */
public class Incidencia {

    /** Valores del ENUM incidencia.tipo (con la etiqueta que se muestra). */
    public static final String[][] TIPOS = {
        {"Seguridad", "Seguridad"}, {"Medica", "Médica"}, {"Acceso denegado", "Acceso denegado"},
        {"Asiento en disputa", "Asiento en disputa"}, {"Objeto perdido", "Objeto perdido"}, {"Otro", "Otro"}
    };
    public static final String[] GRAVEDADES = {"Baja", "Media", "Alta"};
    public static final String[] ESTADOS = {"Abierta", "En atencion", "Cerrada"};

    private Long id;
    private Long idEvento;
    private String evento;
    private String tipo;
    private String descripcion;
    private String gravedad;
    private String puerta;
    private String estado;
    private String observaciones;
    private LocalDateTime fechaHora;
    private Long idUsuario;
    private String usuario;

    public String getFechaHoraTexto() { return Formatos.fechaHora(fechaHora); }

    /** "En atencion" se guarda sin tilde (ENUM); en pantalla se muestra con tilde. */
    public String getEstadoTexto() { return "En atencion".equals(estado) ? "En atención" : estado; }

    public String getTipoTexto() { return "Medica".equals(tipo) ? "Médica" : tipo; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }
    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
