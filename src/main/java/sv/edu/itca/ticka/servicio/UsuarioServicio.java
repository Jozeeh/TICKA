package sv.edu.itca.ticka.servicio;

import java.util.Arrays;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * Reglas de negocio de las cuentas del personal interno (RF-38): creación,
 * edición, restablecimiento de contraseña, activación y desbloqueo. Las
 * cuentas de Cliente se crean por autorregistro en el portal del cliente.
 */
public class UsuarioServicio {

    private final UsuarioDAO usuarioDAO;

    public UsuarioServicio(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /** Crea el primer Administrador; solo funciona si todavía no existe ninguno. */
    public void crearAdministradorInicial(String username, String nombre, String clave, String confirmacion) {
        if (usuarioDAO.existeAdministradorActivo()) {
            throw new ServicioException("El sistema ya tiene un administrador. Inicie sesión.");
        }
        Usuario u = new Usuario();
        u.setUsername(normalizar(username));
        u.setNombre(nombre == null ? null : nombre.trim());
        u.setRol(Rol.ADMINISTRADOR);
        crear(u, clave, confirmacion);
    }

    /** Crea una cuenta de personal (Organizador, Control de acceso o Administrador). */
    public void crearPersonal(Usuario u, String clave, String confirmacion) {
        validarRolPersonal(u.getRol());
        u.setUsername(normalizar(u.getUsername()));
        crear(u, clave, confirmacion);
    }

    private void crear(Usuario u, String clave, String confirmacion) {
        validarDatos(u);
        String errorClave = ValidadorUsuario.validarClave(clave, confirmacion);
        if (errorClave != null) {
            throw new ServicioException(errorClave);
        }
        u.setPasswordHash(SeguridadClave.generarHash(clave));
        u.setActivo(true);
        usuarioDAO.insertar(u);
    }

    /**
     * Actualiza nombre, rol y estado. idSesion es el administrador que hace el
     * cambio: no puede quitarse a sí mismo el rol ni desactivarse.
     */
    public void actualizar(Usuario u, long idSesion) {
        Usuario actual = usuarioDAO.buscarPorId(u.getId())
                .orElseThrow(() -> new ServicioException("El usuario no existe."));
        if (actual.getRol() == Rol.CLIENTE) {
            throw new ServicioException("Las cuentas de cliente se gestionan desde la sección Clientes.");
        }
        validarRolPersonal(u.getRol());
        String errorNombre = ValidadorUsuario.validarNombre(u.getNombre());
        if (errorNombre != null) {
            throw new ServicioException(errorNombre);
        }
        boolean dejaDeSerAdminActivo = actual.isAdministrador() && actual.isActivo()
                && (u.getRol() != Rol.ADMINISTRADOR || !u.isActivo());
        if (dejaDeSerAdminActivo) {
            if (actual.getId() == idSesion) {
                throw new ServicioException("No puede quitarse a sí mismo el rol de administrador ni desactivarse.");
            }
            if (usuarioDAO.contarAdministradoresActivos() <= 1) {
                throw new ServicioException("Debe quedar al menos un administrador activo.");
            }
        }
        usuarioDAO.actualizar(u);
    }

    public void restablecerClave(long id, String clave, String confirmacion) {
        String error = ValidadorUsuario.validarClave(clave, confirmacion);
        if (error != null) {
            throw new ServicioException(error);
        }
        usuarioDAO.cambiarClave(id, SeguridadClave.generarHash(clave));
    }

    /** Activa o desactiva una cuenta (personal o cliente). */
    public void cambiarEstado(long id, boolean activo, long idSesion) {
        Usuario actual = usuarioDAO.buscarPorId(id)
                .orElseThrow(() -> new ServicioException("El usuario no existe."));
        if (!activo && actual.isAdministrador() && actual.isActivo()) {
            if (id == idSesion) {
                throw new ServicioException("No puede desactivar su propia cuenta.");
            }
            if (usuarioDAO.contarAdministradoresActivos() <= 1) {
                throw new ServicioException("Debe quedar al menos un administrador activo.");
            }
        }
        usuarioDAO.cambiarEstado(id, activo);
    }

    public void desbloquear(long id) {
        usuarioDAO.reiniciarIntentos(id);
    }

    private void validarDatos(Usuario u) {
        String error = ValidadorUsuario.validarUsername(u.getUsername());
        if (error == null) {
            error = ValidadorUsuario.validarNombre(u.getNombre());
        }
        if (error != null) {
            throw new ServicioException(error);
        }
        if (usuarioDAO.existeUsername(u.getUsername(), null)) {
            throw new ServicioException("El nombre de usuario ya está registrado.");
        }
    }

    private void validarRolPersonal(Rol rol) {
        if (rol == null || !Arrays.asList(Rol.rolesPersonal()).contains(rol)) {
            throw new ServicioException("Seleccione un rol válido para el personal.");
        }
    }

    private static String normalizar(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }
}
