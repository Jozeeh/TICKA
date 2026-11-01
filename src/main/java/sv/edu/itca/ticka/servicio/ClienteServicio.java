package sv.edu.itca.ticka.servicio;

import sv.edu.itca.ticka.dao.ClienteDAO;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.modelo.Cliente;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * Cuenta del cliente (RF-13, RF-14): autorregistro, edición del perfil y
 * cambio de contraseña. Impide cuentas duplicadas por usuario, documento o
 * correo.
 */
public class ClienteServicio {

    private final ClienteDAO clienteDAO;
    private final UsuarioDAO usuarioDAO;

    public ClienteServicio(ClienteDAO clienteDAO, UsuarioDAO usuarioDAO) {
        this.clienteDAO = clienteDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /** Crea la cuenta (usuario con rol Cliente + datos del cliente). Devuelve el usuario creado. */
    public Usuario registrar(String username, String clave, String confirmacion, Cliente c) {
        String usuarioNormalizado = username == null ? null : username.trim().toLowerCase();
        normalizar(c);
        String error = ValidadorUsuario.validarUsername(usuarioNormalizado);
        if (error == null) {
            error = ValidadorCliente.validar(c);
        }
        if (error == null) {
            error = ValidadorUsuario.validarClave(clave, confirmacion);
        }
        if (error != null) {
            throw new ServicioException(error);
        }
        if (usuarioDAO.existeUsername(usuarioNormalizado, null)) {
            throw new ServicioException("El nombre de usuario ya está registrado. Elija otro.");
        }
        validarUnicos(c, null);

        Usuario u = new Usuario();
        u.setUsername(usuarioNormalizado);
        u.setNombre(c.getNombreCompleto());
        u.setRol(Rol.CLIENTE);
        u.setActivo(true);
        u.setPasswordHash(SeguridadClave.generarHash(clave));
        clienteDAO.registrar(u, c);
        return u;
    }

    /** Actualiza nombres, apellidos, correo y teléfono (el documento no se cambia). */
    public void actualizarPerfil(Cliente c) {
        normalizar(c);
        String error = ValidadorCliente.validar(c);
        if (error != null) {
            throw new ServicioException(error);
        }
        validarUnicos(c, c.getId());
        clienteDAO.actualizarPerfil(c);
    }

    /** Cambia la contraseña verificando primero la actual. */
    public void cambiarClave(long idUsuario, String actual, String nueva, String confirmacion) {
        Usuario u = usuarioDAO.buscarPorId(idUsuario)
                .orElseThrow(() -> new ServicioException("La cuenta no existe."));
        if (!SeguridadClave.verificar(actual, u.getPasswordHash())) {
            throw new ServicioException("La contraseña actual no es correcta.");
        }
        String error = ValidadorUsuario.validarClave(nueva, confirmacion);
        if (error != null) {
            throw new ServicioException(error);
        }
        usuarioDAO.cambiarClave(idUsuario, SeguridadClave.generarHash(nueva));
    }

    private void validarUnicos(Cliente c, Long excluirId) {
        if (clienteDAO.existeDocumento(c.getDocumento(), excluirId)) {
            throw new ServicioException("Ya existe una cuenta con ese documento.");
        }
        if (clienteDAO.existeCorreo(c.getCorreo(), excluirId)) {
            throw new ServicioException("Ya existe una cuenta con ese correo.");
        }
    }

    private static void normalizar(Cliente c) {
        c.setNombres(recortar(c.getNombres()));
        c.setApellidos(recortar(c.getApellidos()));
        c.setDocumento(c.getDocumento() == null ? null : c.getDocumento().trim().toUpperCase());
        c.setCorreo(c.getCorreo() == null ? null : c.getCorreo().trim().toLowerCase());
        String tel = recortar(c.getTelefono());
        c.setTelefono(tel == null || tel.isEmpty() ? null : tel);
    }

    private static String recortar(String s) {
        return s == null ? null : s.trim();
    }
}
