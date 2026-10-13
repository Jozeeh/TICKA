package sv.edu.itca.ticka.modelo;

/** Catálogo para clasificar eventos (tabla categoria). */
public class Categoria {

    private Long id;
    private String nombre;
    /** Cantidad de eventos que la usan (solo lectura). */
    private int totalEventos;

    public Categoria() {
    }

    public Categoria(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getTotalEventos() { return totalEventos; }
    public void setTotalEventos(int totalEventos) { this.totalEventos = totalEventos; }
}
