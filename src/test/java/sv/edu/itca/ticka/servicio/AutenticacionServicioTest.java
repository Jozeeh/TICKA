package sv.edu.itca.ticka.servicio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import sv.edu.itca.ticka.dao.UsuarioDAO;
import sv.edu.itca.ticka.modelo.Rol;
import sv.edu.itca.ticka.modelo.Usuario;

/**
 * Pruebas del inicio de sesión y del bloqueo por intentos fallidos usando un
 * DAO en memoria (sin MySQL).
 */
public class AutenticacionServicioTest {

    private UsuarioDAOEnMemoria dao;
    private AutenticacionServicio servicio;

    @Before
    public void preparar() {
        dao = new UsuarioDAOEnMemoria();
        Usuario u = new Usuario();
        u.setUsername("organizador");
        u.setNombre("Organizador de prueba");
        u.setRol(Rol.ORGANIZADOR);
        u.setPasswordHash(SeguridadClave.generarHash("Ticka2026"));
        dao.insertar(u);
        servicio = new AutenticacionServicio(dao);
    }

    @Test
    public void credencialesCorrectasDevuelvenUsuarioSinHash() {
        Usuario u = servicio.autenticar("organizador", "Ticka2026");
        assertEquals(Rol.ORGANIZADOR, u.getRol());
        assertNull(u.getPasswordHash());
    }

    @Test
    public void bloqueaLaCuentaAlQuintoIntentoFallido() {
        for (int i = 1; i <= AutenticacionServicio.MAX_INTENTOS; i++) {
            try {
                servicio.autenticar("organizador", "incorrecta1");
                fail("Debió rechazar la contraseña");
            } catch (ServicioException esperado) {
                // cada intento fallido suma uno
            }
        }
        assertTrue(dao.buscarPorUsername("organizador").get().isBloqueado());
        try {
            servicio.autenticar("organizador", "Ticka2026");
            fail("Una cuenta bloqueada no debe entrar aunque la clave sea correcta");
        } catch (ServicioException esperado) {
            assertTrue(esperado.getMessage().contains("bloqueada"));
        }
    }

    @Test(expected = ServicioException.class)
    public void cuentaDesactivadaNoEntra() {
        dao.cambiarEstado(1L, false);
        servicio.autenticar("organizador", "Ticka2026");
    }

    /** Implementación mínima de UsuarioDAO para las pruebas. */
    private static class UsuarioDAOEnMemoria implements UsuarioDAO {

        private final Map<Long, Usuario> datos = new HashMap<>();

        @Override
        public Optional<Usuario> buscarPorUsername(String username) {
            return datos.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(long id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public List<Usuario> listarPersonal() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public boolean existeAdministradorActivo() {
            return contarAdministradoresActivos() > 0;
        }

        @Override
        public int contarAdministradoresActivos() {
            return (int) datos.values().stream().filter(u -> u.isAdministrador() && u.isActivo()).count();
        }

        @Override
        public boolean existeUsername(String username, Long excluirId) {
            return buscarPorUsername(username).isPresent();
        }

        @Override
        public long insertar(Usuario u) {
            long id = datos.size() + 1L;
            u.setId(id);
            datos.put(id, u);
            return id;
        }

        @Override
        public boolean actualizar(Usuario u) {
            datos.put(u.getId(), u);
            return true;
        }

        @Override
        public boolean cambiarClave(long id, String hash) {
            datos.get(id).setPasswordHash(hash);
            return true;
        }

        @Override
        public boolean cambiarEstado(long id, boolean activo) {
            datos.get(id).setActivo(activo);
            return true;
        }

        @Override
        public void registrarIntentoFallido(long id, int intentos, LocalDateTime hasta) {
            datos.get(id).setIntentosFallidos(intentos);
            datos.get(id).setBloqueadoHasta(hasta);
        }

        @Override
        public void reiniciarIntentos(long id) {
            datos.get(id).setIntentosFallidos(0);
            datos.get(id).setBloqueadoHasta(null);
        }
    }
}
