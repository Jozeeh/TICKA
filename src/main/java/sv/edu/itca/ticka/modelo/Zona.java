package sv.edu.itca.ticka.modelo;

import java.util.ArrayList;
import java.util.List;

/** Sección de un recinto que agrupa asientos (tabla zona). */
public class Zona {

    private Long id;
    private Long idRecinto;
    private String nombre;
    private String descripcion;

    // Datos de lectura
    private int totalAsientos;
    private int asientosActivos;
    /** Asientos agrupados por fila para dibujar la cuadrícula. */
    private List<FilaAsientos> filas = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdRecinto() { return idRecinto; }
    public void setIdRecinto(Long idRecinto) { this.idRecinto = idRecinto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getTotalAsientos() { return totalAsientos; }
    public void setTotalAsientos(int totalAsientos) { this.totalAsientos = totalAsientos; }

    public int getAsientosActivos() { return asientosActivos; }
    public void setAsientosActivos(int asientosActivos) { this.asientosActivos = asientosActivos; }

    public List<FilaAsientos> getFilas() { return filas; }
    public void setFilas(List<FilaAsientos> filas) { this.filas = filas; }
}
