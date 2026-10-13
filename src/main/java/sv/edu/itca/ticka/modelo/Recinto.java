package sv.edu.itca.ticka.modelo;

/** Lugar físico donde se realizan los eventos (tabla recinto). */
public class Recinto {

    /** Valores del ENUM recinto.tipo. */
    public static final String[] TIPOS = {"Estadio", "Arena", "Teatro", "Otro"};

    private Long id;
    private String nombre;
    private String direccion;
    private String tipo = "Otro";
    private boolean activo = true;

    // Datos de lectura
    private int totalZonas;
    private int totalAsientos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getTotalZonas() { return totalZonas; }
    public void setTotalZonas(int totalZonas) { this.totalZonas = totalZonas; }

    public int getTotalAsientos() { return totalAsientos; }
    public void setTotalAsientos(int totalAsientos) { this.totalAsientos = totalAsientos; }
}
