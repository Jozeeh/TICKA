package sv.edu.itca.ticka.modelo;

/**
 * Los 4 roles del sistema. El nombre coincide con la columna rol.nombre de
 * la base de datos, por eso el DAO convierte con desdeNombre().
 */
public enum Rol {

    ORGANIZADOR("Organizador"),
    CLIENTE("Cliente"),
    CONTROL_ACCESO("Control de acceso"),
    ADMINISTRADOR("Administrador");

    private final String nombre;

    Rol(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /** Convierte el texto guardado en la tabla rol al valor del enum. */
    public static Rol desdeNombre(String nombre) {
        for (Rol rol : values()) {
            if (rol.nombre.equalsIgnoreCase(nombre)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + nombre);
    }

    /** Roles que el Administrador puede asignar al personal interno. */
    public static Rol[] rolesPersonal() {
        return new Rol[]{ORGANIZADOR, CONTROL_ACCESO, ADMINISTRADOR};
    }
}
