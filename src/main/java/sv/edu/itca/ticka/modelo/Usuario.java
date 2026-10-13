package sv.edu.itca.ticka.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cuenta para iniciar sesión (tabla usuario). La sesión HTTP guarda una
 * copia de este objeto sin el hash de la contraseña.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private String username;
    private String passwordHash;
    private String nombre;
    private Rol rol;
    private boolean activo = true;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime creadoEn;

    /** true si la cuenta está bloqueada temporalmente por intentos fallidos. */
    public boolean isBloqueado() {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(LocalDateTime.now());
    }

    public boolean isAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    /** Organizador y Administrador pueden gestionar eventos y recintos. */
    public boolean isGestorEventos() {
        return rol == Rol.ORGANIZADOR || rol == Rol.ADMINISTRADOR;
    }

    public String getBloqueadoHastaTexto() {
        return bloqueadoHasta == null ? "" : bloqueadoHasta.format(FORMATO);
    }

    public String getCreadoEnTexto() {
        return creadoEn == null ? "" : creadoEn.format(FORMATO);
    }

    /** Copia segura para guardar en la sesión (sin hash de contraseña). */
    public Usuario copiaParaSesion() {
        Usuario u = new Usuario();
        u.id = id;
        u.username = username;
        u.nombre = nombre;
        u.rol = rol;
        u.activo = activo;
        return u;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
