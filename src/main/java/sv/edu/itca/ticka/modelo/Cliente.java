package sv.edu.itca.ticka.modelo;

import java.math.BigDecimal;
import sv.edu.itca.ticka.util.Formatos;

/**
 * Comprador con cuenta propia (tabla cliente + su usuario). En la vista del
 * Administrador incluye el resumen de compras.
 */
public class Cliente {

    private Long id;
    private Long idUsuario;
    private String username;
    private String nombres;
    private String apellidos;
    private String documento;
    private String correo;
    private String telefono;
    private boolean activo;

    // Datos de lectura
    private int compras;
    private int boletos;
    private BigDecimal totalGastado = BigDecimal.ZERO;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public String getTotalGastadoTexto() {
        return Formatos.dinero(totalGastado);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getCompras() { return compras; }
    public void setCompras(int compras) { this.compras = compras; }

    public int getBoletos() { return boletos; }
    public void setBoletos(int boletos) { this.boletos = boletos; }

    public BigDecimal getTotalGastado() { return totalGastado; }
    public void setTotalGastado(BigDecimal totalGastado) { this.totalGastado = totalGastado; }
}
