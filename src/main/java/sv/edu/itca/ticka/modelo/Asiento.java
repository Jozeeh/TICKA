package sv.edu.itca.ticka.modelo;

/**
 * Asiento de la plantilla de un recinto (tabla asiento) o, en el mapa de
 * un evento, su estado en ese evento (tabla evento_asiento). Para el mapa
 * se usan idEventoAsiento y estado; para la plantilla, id y activo.
 */
public class Asiento {

    private Long id;
    private Long idZona;
    private String fila;
    private int numero;
    private boolean activo = true;

    // Solo en el mapa de un evento
    private Long idEventoAsiento;
    private String estado;

    /** Etiqueta corta: B-12 */
    public String getEtiqueta() {
        return fila + "-" + numero;
    }

    /** Clase CSS del asiento en la vista de plantilla. */
    public String getClasePlantilla() {
        return activo ? "Activo" : "Inactivo";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdZona() { return idZona; }
    public void setIdZona(Long idZona) { this.idZona = idZona; }

    public String getFila() { return fila; }
    public void setFila(String fila) { this.fila = fila; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Long getIdEventoAsiento() { return idEventoAsiento; }
    public void setIdEventoAsiento(Long idEventoAsiento) { this.idEventoAsiento = idEventoAsiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
