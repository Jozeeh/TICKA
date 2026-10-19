package sv.edu.itca.ticka.controlador.admin;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.itca.ticka.dao.DAOException;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.dao.impl.UsuarioDAOImpl;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;
import sv.edu.itca.ticka.servicio.ServicioException;
import sv.edu.itca.ticka.servicio.UsuarioServicio;
import sv.edu.itca.ticka.servicio.ValidadorUsuario;
import sv.edu.itca.ticka.util.Parametros;
import sv.edu.itca.ticka.util.Sesion;

/**
 * Usuarios del personal interno (RF-37 a RF-40): Organizador, Control de
 * acceso y Administrador. Permite crear, editar nombre/rol/estado,
 * restablecer la contraseña y desbloquear cuentas bloqueadas por intentos.
 */
@WebServlet("/admin/usuarios")
public class UsuarioServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/vistas/admin/usuarios/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/admin/usuarios/formulario.jsp";

    private UsuarioDAO usuarioDAO;
    private UsuarioServicio usuarioServicio;

    @Override
    public void init() {
        usuarioDAO = new UsuarioDAOImpl();
        usuarioServicio = new UsuarioServicio(usuarioDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        try {
            if ("nuevo".equals(accion)) {
                Usuario u = new Usuario();
                u.setRol(Rol.ORGANIZADOR);
                mostrarFormulario(req, resp, u);
            } else if ("editar".equals(accion)) {
                Long id = Parametros.enteroLargo(req.getParameter("id"));
                Usuario u = id == null ? null : usuarioDAO.buscarPorId(id).orElse(null);
                if (u == null || u.getRol() == Rol.CLIENTE) {
                    Sesion.error(req, "El usuario no existe o es un cliente.");
                    resp.sendRedirect(req.getContextPath() + "/admin/usuarios");
                    return;
                }
                mostrarFormulario(req, resp, u);
            } else {
                req.setAttribute("usuarios", usuarioDAO.listarPersonal());
                req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
            }
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            req.setAttribute("error", "No se pudo cargar los usuarios. Verifique la conexión con MySQL.");
            req.setAttribute("usuarios", Collections.emptyList());
            req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
        }
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Usuario u)
            throws ServletException, IOException {
        req.setAttribute("usuario", u);
        req.setAttribute("roles", Rol.rolesPersonal());
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = req.getParameter("accion");
        Long id = Parametros.enteroLargo(req.getParameter("id"));
        long idSesion = Sesion.usuario(req).getId();
        try {
            switch (accion == null ? "" : accion) {
                case "guardar":
                    guardar(req, resp, idSesion);
                    return;
                case "estado":
                    boolean activar = "si".equals(req.getParameter("activar"));
                    usuarioServicio.cambiarEstado(requerido(id), activar, idSesion);
                    Sesion.exito(req, activar ? "Usuario activado." : "Usuario desactivado.");
                    break;
                case "desbloquear":
                    usuarioServicio.desbloquear(requerido(id));
                    Sesion.exito(req, "Cuenta desbloqueada.");
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
            }
        } catch (ServicioException ex) {
            Sesion.error(req, ex.getMessage());
        } catch (DAOException ex) {
            log("Error de persistencia", ex);
            Sesion.error(req, "No se pudo completar la operación. Intente de nuevo.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/usuarios");
    }

    private static long requerido(Long id) {
        if (id == null) {
            throw new ServicioException("Usuario no válido.");
        }
        return id;
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp, long idSesion)
            throws ServletException, IOException {
        Usuario u = new Usuario();
        u.setId(Parametros.enteroLargo(req.getParameter("id")));
        u.setUsername(Parametros.texto(req.getParameter("username")));
        u.setNombre(Parametros.texto(req.getParameter("nombre")));
        u.setRol(leerRol(req.getParameter("rol")));
        u.setActivo(u.getId() == null || "si".equals(req.getParameter("activo")));
        String clave = req.getParameter("clave");
        String confirmacion = req.getParameter("confirmacion");
        try {
            if (u.getId() == null) {
                usuarioServicio.crearPersonal(u, clave, confirmacion);
                Sesion.exito(req, "Usuario " + u.getUsername() + " creado con rol " + u.getRol().getNombre() + ".");
            } else {
                boolean cambiaClave = clave != null && !clave.isEmpty();
                // Se valida la nueva contraseña antes de guardar cualquier cambio.
                String errorClave = cambiaClave ? ValidadorUsuario.validarClave(clave, confirmacion) : null;
                if (errorClave != null) {
                    throw new ServicioException(errorClave);
                }
                usuarioServicio.actualizar(u, idSesion);
                if (cambiaClave) {
                    usuarioServicio.restablecerClave(u.getId(), clave, confirmacion);
                    Sesion.exito(req, "Usuario actualizado y contraseña restablecida.");
                } else {
                    Sesion.exito(req, "Usuario actualizado.");
                }
            }
            resp.sendRedirect(req.getContextPath() + "/admin/usuarios");
        } catch (ServicioException ex) {
            if (u.getId() != null) {
                // El username no se edita: se recupera para volver a mostrarlo.
                usuarioDAO.buscarPorId(u.getId()).ifPresent(actual -> u.setUsername(actual.getUsername()));
            }
            req.setAttribute("error", ex.getMessage());
            mostrarFormulario(req, resp, u);
        }
    }

    private static Rol leerRol(String valor) {
        try {
            return valor == null ? null : Rol.valueOf(valor);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
