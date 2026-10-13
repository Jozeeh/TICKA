package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.ticka.util.Formatos;

/**
 * Espectáculo en vivo (tabla evento): concierto, partido, ópera o teatro.
 * Incluye datos de lectura de otras tablas (nombre de la categoría y del
 * recinto, asientos totales y vendidos) para los listados.
 */
public class Evento {

    private Long id;
    private String nombre;
    private TipoEvento tipo;
    private String descripcion;
    private String participantes;
    private String imagen;
    private Long idCategoria;
    private Long idRecinto;
    private LocalDateTime aperturaPuertas;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private EstadoEvento estado = EstadoEvento.BORRADOR;
    private Long creadoPor;

    // Datos de lectura (JOIN / subconsultas)
    private String categoria;
    private String recinto;
    private int totalAsientos;
    private int vendidos;

    // Solo en la cartelera de la página de inicio
    private int disponibles;
    private BigDecimal precioMinimo;
    private List<PrecioZona> precios = new ArrayList<>();

    public int getPorcentajeVendido() {
        return Formatos.porcentaje(vendidos, totalAsientos);
    }

    public String getPrecioMinimoTexto() { return Formatos.dinero(precioMinimo); }

    /** Día y mes para el bloque de fecha de la tarjeta: "21" y "NOV". */
    public String getDia() { return fechaInicio == null ? "" : String.valueOf(fechaInicio.getDayOfMonth()); }
    public String getMesCorto() { return Formatos.mesCorto(fechaInicio); }
    public String getDiaSemana() { return Formatos.diaSemana(fechaInicio); }

    /** Pocos asientos libres: menos del 15 % del total. */
    public boolean isUltimosAsientos() {
        return totalAsientos > 0 && disponibles > 0 && disponibles * 100 < totalAsientos * 15;
    }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    public BigDecimal getPrecioMinimo() { return precioMinimo; }
    public void setPrecioMinimo(BigDecimal precioMinimo) { this.precioMinimo = precioMinimo; }

    public List<PrecioZona> getPrecios() { return precios; }
    public void setPrecios(List<PrecioZona> precios) { this.precios = precios; }

    public String getFechaInicioTexto() { return Formatos.fechaHora(fechaInicio); }
    public String getFechaFinTexto() { return Formatos.fechaHora(fechaFin); }
    public String getAperturaPuertasTexto() { return Formatos.fechaHora(aperturaPuertas); }
    public String getFechaTexto() { return Formatos.fecha(fechaInicio); }
    public String getHorarioTexto() { return Formatos.hora(fechaInicio) + " – " + Formatos.hora(fechaFin); }

    /** Valores para los campos datetime-local del formulario. */
    public String getFechaInicioEntrada() { return Formatos.entrada(fechaInicio); }
    public String getFechaFinEntrada() { return Formatos.entrada(fechaFin); }
    public String getAperturaPuertasEntrada() { return Formatos.entrada(aperturaPuertas); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getParticipantes() { return participantes; }
    public void setParticipantes(String participantes) { this.participantes = participantes; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public Long getIdRecinto() { return idRecinto; }
    public void setIdRecinto(Long idRecinto) { this.idRecinto = idRecinto; }

    public LocalDateTime getAperturaPuertas() { return aperturaPuertas; }
    public void setAperturaPuertas(LocalDateTime aperturaPuertas) { this.aperturaPuertas = aperturaPuertas; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public EstadoEvento getEstado() { return estado; }
    public void setEstado(EstadoEvento estado) { this.estado = estado; }

    public Long getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Long creadoPor) { this.creadoPor = creadoPor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getRecinto() { return recinto; }
    public void setRecinto(String recinto) { this.recinto = recinto; }

    public int getTotalAsientos() { return totalAsientos; }
    public void setTotalAsientos(int totalAsientos) { this.totalAsientos = totalAsientos; }

    public int getVendidos() { return vendidos; }
    public void setVendidos(int vendidos) { this.vendidos = vendidos; }
}
