package sv.edu.itca.ticka.filtro;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;

/** Pruebas de la matriz de acceso por rol (sección 2.2 de requerimientos). */
public class AutenticacionFilterTest {

    private static Usuario con(Rol rol) {
        Usuario u = new Usuario();
        u.setRol(rol);
        return u;
    }

    @Test
    public void administradorEntraATodo() {
        assertTrue(AutenticacionFilter.tienePermiso(con(Rol.ADMINISTRADOR), "/admin/usuarios"));
        assertTrue(AutenticacionFilter.tienePermiso(con(Rol.ADMINISTRADOR), "/organizador/eventos"));
    }

    @Test
    public void organizadorNoEntraAAdministracion() {
        assertTrue(AutenticacionFilter.tienePermiso(con(Rol.ORGANIZADOR), "/organizador/eventos"));
        assertFalse(AutenticacionFilter.tienePermiso(con(Rol.ORGANIZADOR), "/admin/dashboard"));
    }

    @Test
    public void reportesSoloParaOrganizadorYAdministrador() {
        assertTrue(AutenticacionFilter.tienePermiso(con(Rol.ORGANIZADOR), "/reportes"));
        assertTrue(AutenticacionFilter.tienePermiso(con(Rol.ADMINISTRADOR), "/reportes"));
        assertFalse(AutenticacionFilter.tienePermiso(con(Rol.CLIENTE), "/reportes"));
    }

    @Test
    public void clienteYControlDeAccesoNoGestionanEventos() {
        assertFalse(AutenticacionFilter.tienePermiso(con(Rol.CLIENTE), "/organizador/eventos"));
        assertFalse(AutenticacionFilter.tienePermiso(con(Rol.CONTROL_ACCESO), "/reportes"));
        assertTrue(AutenticacionFilter.tienePermiso(con(Rol.CLIENTE), "/panel"));
    }
}
